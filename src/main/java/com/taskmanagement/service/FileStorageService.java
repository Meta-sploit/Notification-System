package com.taskmanagement.service;

import com.taskmanagement.dto.FileAttachmentDTO;
import com.taskmanagement.exception.FileStorageException;
import com.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.model.AuditLog;
import com.taskmanagement.model.FileAttachment;
import com.taskmanagement.model.Task;
import com.taskmanagement.repository.FileAttachmentRepository;
import com.taskmanagement.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final FileAttachmentRepository fileAttachmentRepository;
    private final TaskRepository taskRepository;
    private final AuditLogService auditLogService;

    @Value("${app.storage.type:LOCAL}")
    private String storageType;

    @Value("${app.storage.local.upload-dir:./uploads}")
    private String localUploadDir;

    @Value("${app.storage.s3.bucket-name:}")
    private String s3BucketName;

    @Value("${app.storage.s3.region:us-east-1}")
    private String s3Region;

    @Value("${app.storage.s3.access-key:}")
    private String s3AccessKey;

    @Value("${app.storage.s3.secret-key:}")
    private String s3SecretKey;

    @Value("${app.storage.s3.presigned-url-expiration:3600}")
    private long presignedUrlExpiration;

    @Transactional
    public FileAttachmentDTO uploadFile(MultipartFile file, Long taskId, Long uploadedById) {
        log.info("Uploading file: {} for task: {}", file.getOriginalFilename(), taskId);

        // Validate file
        if (file.isEmpty()) {
            throw new FileStorageException("Cannot upload empty file");
        }

        // Get task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        // Generate unique file key
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileKey = generateFileKey(originalFilename);

        try {
            // Upload based on storage type
            if ("S3".equalsIgnoreCase(storageType)) {
                uploadToS3(file, fileKey);
            } else {
                uploadToLocal(file, fileKey);
            }

            // Save file attachment record
            FileAttachment fileAttachment = FileAttachment.builder()
                    .fileName(originalFilename)
                    .fileKey(fileKey)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .task(task)
                    .build();

            FileAttachment savedAttachment = fileAttachmentRepository.save(fileAttachment);

            // Audit log
            auditLogService.log("FILE", savedAttachment.getId(), AuditLog.AuditAction.FILE_UPLOAD,
                    "SYSTEM", null, originalFilename, "File uploaded for task " + taskId);

            log.info("File uploaded successfully: {}", fileKey);
            return convertToDTO(savedAttachment);

        } catch (IOException e) {
            throw new FileStorageException("Failed to upload file: " + originalFilename, e);
        }
    }

    @Transactional(readOnly = true)
    public FileAttachmentDTO getFileById(Long id) {
        FileAttachment attachment = fileAttachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FileAttachment", "id", id));
        return convertToDTO(attachment);
    }

    @Transactional(readOnly = true)
    public List<FileAttachmentDTO> getFilesByTaskId(Long taskId) {
        return fileAttachmentRepository.findByTaskId(taskId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFile(Long id) {
        log.info("Deleting file: {}", id);

        FileAttachment attachment = fileAttachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FileAttachment", "id", id));

        try {
            // Delete from storage
            if ("S3".equalsIgnoreCase(storageType)) {
                deleteFromS3(attachment.getFileKey());
            } else {
                deleteFromLocal(attachment.getFileKey());
            }

            // Delete record
            fileAttachmentRepository.delete(attachment);

            // Audit log
            auditLogService.log("FILE", id, AuditLog.AuditAction.FILE_DELETE,
                    "SYSTEM", attachment.getFileName(), null, "File deleted");

            log.info("File deleted successfully: {}", id);

        } catch (Exception e) {
            throw new FileStorageException("Failed to delete file: " + attachment.getFileName(), e);
        }
    }

    public String generatePresignedUrl(String fileKey) {
        if ("S3".equalsIgnoreCase(storageType)) {
            return generateS3PresignedUrl(fileKey);
        } else {
            // For local storage, return a relative path or endpoint
            return "/api/files/download/" + fileKey;
        }
    }

    private void uploadToLocal(MultipartFile file, String fileKey) throws IOException {
        Path uploadPath = Paths.get(localUploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileKey);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
    }

    private void uploadToS3(MultipartFile file, String fileKey) throws IOException {
        S3Client s3Client = createS3Client();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3BucketName)
                .key(fileKey)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        s3Client.close();
    }

    private void deleteFromLocal(String fileKey) throws IOException {
        Path filePath = Paths.get(localUploadDir).resolve(fileKey);
        Files.deleteIfExists(filePath);
    }

    private void deleteFromS3(String fileKey) {
        S3Client s3Client = createS3Client();
        s3Client.deleteObject(builder -> builder.bucket(s3BucketName).key(fileKey));
        s3Client.close();
    }

    private String generateS3PresignedUrl(String fileKey) {
        S3Presigner presigner = createS3Presigner();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3BucketName)
                .key(fileKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
        String url = presignedRequest.url().toString();

        presigner.close();
        return url;
    }

    private S3Client createS3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(s3AccessKey, s3SecretKey);

        return S3Client.builder()
                .region(Region.of(s3Region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    private S3Presigner createS3Presigner() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(s3AccessKey, s3SecretKey);

        return S3Presigner.builder()
                .region(Region.of(s3Region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    private String generateFileKey(String originalFilename) {
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        return UUID.randomUUID().toString() + extension;
    }

    private FileAttachmentDTO convertToDTO(FileAttachment attachment) {
        FileAttachmentDTO dto = FileAttachmentDTO.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileKey(attachment.getFileKey())
                .contentType(attachment.getContentType())
                .fileSize(attachment.getFileSize())
                .taskId(attachment.getTask().getId())
                .uploadedAt(attachment.getUploadedAt())
                .build();

        // Generate download URL
        dto.setDownloadUrl(generatePresignedUrl(attachment.getFileKey()));

        return dto;
    }
}

