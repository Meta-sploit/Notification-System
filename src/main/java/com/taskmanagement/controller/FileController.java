package com.taskmanagement.controller;

import com.taskmanagement.dto.FileAttachmentDTO;
import com.taskmanagement.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File Management", description = "APIs for managing file attachments")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @Operation(summary = "Upload a file attachment for a task (All authenticated users)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileAttachmentDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("taskId") Long taskId,
            @RequestParam(value = "uploadedById", required = false) Long uploadedById) {
        FileAttachmentDTO uploadedFile = fileStorageService.uploadFile(file, taskId, uploadedById);
        return new ResponseEntity<>(uploadedFile, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get file attachment by ID (All authenticated users)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileAttachmentDTO> getFileById(@PathVariable Long id) {
        FileAttachmentDTO file = fileStorageService.getFileById(id);
        return ResponseEntity.ok(file);
    }

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Get all file attachments for a task (All authenticated users)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FileAttachmentDTO>> getFilesByTaskId(@PathVariable Long taskId) {
        List<FileAttachmentDTO> files = fileStorageService.getFilesByTaskId(taskId);
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete file attachment (ADMIN and MANAGER only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        fileStorageService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download-url")
    @Operation(summary = "Get pre-signed download URL for a file (All authenticated users)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> getDownloadUrl(@PathVariable Long id) {
        FileAttachmentDTO file = fileStorageService.getFileById(id);
        return ResponseEntity.ok(file.getDownloadUrl());
    }
}

