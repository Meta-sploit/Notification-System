package com.taskmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileAttachmentDTO {

    private Long id;

    private String fileName;

    private String fileKey;

    private String contentType;

    private Long fileSize;

    private Long taskId;

    private Long uploadedById;

    private String uploadedByName;

    private LocalDateTime uploadedAt;

    private String downloadUrl;  // Pre-signed URL
}

