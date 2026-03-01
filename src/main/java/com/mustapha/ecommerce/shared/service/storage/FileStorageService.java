package com.mustapha.ecommerce.shared.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * File Storage Service - Abstraction Layer
 * 
 * This interface allows switching between storage providers without changing business logic:
 * - AWS S3 (production)
 * - Local filesystem (development)
 * - Azure Blob Storage (alternative)
 * - Google Cloud Storage (alternative)
 * 
 * Design Pattern: Strategy Pattern
 */
public interface FileStorageService {
    
    /**
     * Upload a file
     * 
     * @param file The file to upload
     * @param directory The directory/folder (e.g., "products", "users/avatars")
     * @return The public URL of the uploaded file
     * @throws IOException if upload fails
     */
    String uploadFile(MultipartFile file, String directory) throws IOException;
    
    /**
     * Upload a file from input stream
     * 
     * @param inputStream The input stream
     * @param fileName The file name
     * @param contentType The content type (e.g., "image/jpeg")
     * @param directory The directory/folder
     * @return The public URL of the uploaded file
     * @throws IOException if upload fails
     */
    String uploadFile(InputStream inputStream, String fileName, String contentType, String directory) throws IOException;
    
    /**
     * Delete a file
     * 
     * @param fileUrl The file URL returned by uploadFile()
     * @throws IOException if deletion fails
     */
    void deleteFile(String fileUrl) throws IOException;
    
    /**
     * Delete multiple files
     * 
     * @param fileUrls List of file URLs
     */
    void deleteFiles(List<String> fileUrls);
    
    /**
     * Get a pre-signed URL for temporary access to a private file
     * 
     * @param fileKey The file key (path)
     * @param expirationMinutes URL expiration time in minutes
     * @return Pre-signed URL
     */
    String getPresignedUrl(String fileKey, int expirationMinutes);
    
    /**
     * Check if a file exists
     * 
     * @param fileUrl The file URL
     * @return true if file exists
     */
    boolean fileExists(String fileUrl);
    
    /**
     * Get file size in bytes
     * 
     * @param fileUrl The file URL
     * @return File size in bytes, or -1 if not found
     */
    long getFileSize(String fileUrl);
}
