package com.mustapha.ecommerce.shared.service.storage;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * Local File Storage Implementation
 * 
 * Fallback implementation when AWS S3 is not configured.
 * Used for:
 * - Local development
 * - Testing
 * - Environments without AWS access
 * 
 * Files are stored in: ./uploads/{directory}/{filename}
 * 
 * WARNING: Not suitable for production distributed systems
 * - Files stored on single server only
 * - No automatic backups
 * - No CDN integration
 * - Limited by disk space
 * 
 * For production, use S3FileStorageService
 */
@Service
@ConditionalOnMissingBean(S3Client.class)
public class LocalFileStorageService implements FileStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalFileStorageService.class);
    private static final String UPLOAD_DIR = "uploads";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    
    public LocalFileStorageService() {
        logger.warn("⚠️ Using LOCAL file storage (development mode)");
        logger.warn("   Files will be stored in: ./{}", UPLOAD_DIR);
        logger.warn("   For production, configure AWS S3 credentials");
        
        // Create uploads directory
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Failed to create upload directory: {}", e.getMessage());
        }
    }
    
    @Override
    public String uploadFile(MultipartFile file, String directory) throws IOException {
        validateFile(file);
        
        String originalFilename = file.getOriginalFilename();
        String fileName = generateFileName(originalFilename);
        String relativePath = directory + "/" + fileName;
        
        Path directoryPath = Paths.get(UPLOAD_DIR, directory);
        Files.createDirectories(directoryPath);
        
        Path filePath = Paths.get(UPLOAD_DIR, relativePath);
        
        logger.info("Uploading file locally: path={}, size={} bytes", filePath, file.getSize());
        
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        String fileUrl = "/uploads/" + relativePath;
        logger.info("✅ File uploaded locally: {}", fileUrl);
        
        return fileUrl;
    }
    
    @Override
    public String uploadFile(InputStream inputStream, String fileName, String contentType, String directory) throws IOException {
        String uniqueFileName = generateFileName(fileName);
        String relativePath = directory + "/" + uniqueFileName;
        
        Path directoryPath = Paths.get(UPLOAD_DIR, directory);
        Files.createDirectories(directoryPath);
        
        Path filePath = Paths.get(UPLOAD_DIR, relativePath);
        
        logger.info("Uploading stream locally: path={}", filePath);
        
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        
        String fileUrl = "/uploads/" + relativePath;
        logger.info("✅ Stream uploaded locally: {}", fileUrl);
        
        return fileUrl;
    }
    
    @Override
    public void deleteFile(String fileUrl) throws IOException {
        String relativePath = extractRelativePath(fileUrl);
        Path filePath = Paths.get(UPLOAD_DIR, relativePath);
        
        logger.info("Deleting local file: path={}", filePath);
        
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            logger.info("✅ File deleted: {}", filePath);
        } else {
            logger.warn("File not found for deletion: {}", filePath);
        }
    }
    
    @Override
    public void deleteFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }
        
        logger.info("Deleting {} local files", fileUrls.size());
        
        for (String fileUrl : fileUrls) {
            try {
                deleteFile(fileUrl);
            } catch (IOException e) {
                logger.warn("Failed to delete file: {} - {}", fileUrl, e.getMessage());
            }
        }
    }
    
    @Override
    public String getPresignedUrl(String fileKey, int expirationMinutes) {
        // Local storage doesn't need pre-signed URLs - files are directly accessible
        logger.debug("Pre-signed URL not needed for local storage: {}", fileKey);
        return "/uploads/" + fileKey;
    }
    
    @Override
    public boolean fileExists(String fileUrl) {
        String relativePath = extractRelativePath(fileUrl);
        Path filePath = Paths.get(UPLOAD_DIR, relativePath);
        return Files.exists(filePath);
    }
    
    @Override
    public long getFileSize(String fileUrl) {
        String relativePath = extractRelativePath(fileUrl);
        Path filePath = Paths.get(UPLOAD_DIR, relativePath);
        
        try {
            if (Files.exists(filePath)) {
                return Files.size(filePath);
            }
        } catch (IOException e) {
            logger.error("Error getting file size: {}", e.getMessage());
        }
        
        return -1;
    }
    
    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum allowed: 5 MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new IOException("File type not allowed: " + contentType);
        }
    }
    
    /**
     * Check if content type is allowed
     */
    private boolean isAllowedContentType(String contentType) {
        return contentType.startsWith("image/") || 
               contentType.equals("application/pdf") ||
               contentType.equals("application/json");
    }
    
    /**
     * Generate unique file name
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
    
    /**
     * Extract relative path from file URL
     * /uploads/products/file.jpg → products/file.jpg
     */
    private String extractRelativePath(String fileUrl) {
        if (fileUrl.startsWith("/uploads/")) {
            return fileUrl.substring("/uploads/".length());
        }
        return fileUrl;
    }
}
