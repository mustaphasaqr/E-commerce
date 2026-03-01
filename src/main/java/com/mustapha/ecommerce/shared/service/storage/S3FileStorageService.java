package com.mustapha.ecommerce.shared.service.storage;

import com.mustapha.ecommerce.shared.config.StorageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * AWS S3 File Storage Implementation
 * 
 * Production-ready implementation with:
 * - Automatic file naming (UUID to prevent conflicts)
 * - Directory organization
 * - Public and private file support
 * - Pre-signed URLs for temporary access
 * - Resilience (delete failures don't crash)
 * 
 * AWS S3 Pricing (Free Tier):
 * - 5 GB storage
 * - 20,000 GET requests
 * - 2,000 PUT requests
 * - 15 GB data transfer out
 * 
 * After free tier: ~$0.023 per GB/month
 */
@Service
@ConditionalOnBean(S3Client.class)
public class S3FileStorageService implements FileStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(S3FileStorageService.class);
    
    private final S3Client s3Client;
    private final StorageConfig storageConfig;
    
    public S3FileStorageService(S3Client s3Client, StorageConfig storageConfig) {
        this.s3Client = s3Client;
        this.storageConfig = storageConfig;
        logger.info("✅ S3FileStorageService initialized");
    }
    
    @Override
    public String uploadFile(MultipartFile file, String directory) throws IOException {
        validateFile(file);
        
        String originalFilename = file.getOriginalFilename();
        String fileName = generateFileName(originalFilename);
        String key = directory + "/" + fileName;
        
        logger.info("Uploading file to S3: bucket={}, key={}, size={} bytes", 
                   storageConfig.getBucketName(), key, file.getSize());
        
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(storageConfig.getBucketName())
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            
            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            String fileUrl = buildPublicUrl(key);
            logger.info("✅ File uploaded successfully: {}", fileUrl);
            
            return fileUrl;
            
        } catch (S3Exception e) {
            logger.error("❌ Failed to upload file to S3: {}", e.getMessage());
            throw new IOException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String uploadFile(InputStream inputStream, String fileName, String contentType, String directory) throws IOException {
        String uniqueFileName = generateFileName(fileName);
        String key = directory + "/" + uniqueFileName;
        
        logger.info("Uploading stream to S3: bucket={}, key={}", storageConfig.getBucketName(), key);
        
        try {
            // Note: For production with large files, consider using multipart upload
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(storageConfig.getBucketName())
                    .key(key)
                    .contentType(contentType)
                    .build();
            
            s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, inputStream.available()));
            
            String fileUrl = buildPublicUrl(key);
            logger.info("✅ Stream uploaded successfully: {}", fileUrl);
            
            return fileUrl;
            
        } catch (S3Exception | IOException e) {
            logger.error("❌ Failed to upload stream to S3: {}", e.getMessage());
            throw new IOException("Failed to upload stream to S3: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteFile(String fileUrl) throws IOException {
        String key = extractKeyFromUrl(fileUrl);
        
        logger.info("Deleting file from S3: bucket={}, key={}", storageConfig.getBucketName(), key);
        
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(storageConfig.getBucketName())
                    .key(key)
                    .build();
            
            s3Client.deleteObject(deleteRequest);
            logger.info("✅ File deleted successfully: {}", key);
            
        } catch (S3Exception e) {
            logger.error("❌ Failed to delete file from S3: {}", e.getMessage());
            throw new IOException("Failed to delete file from S3: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }
        
        logger.info("Deleting {} files from S3", fileUrls.size());
        
        for (String fileUrl : fileUrls) {
            try {
                deleteFile(fileUrl);
            } catch (IOException e) {
                // Log but don't fail - continue deleting other files
                logger.warn("Failed to delete file: {} - {}", fileUrl, e.getMessage());
            }
        }
    }
    
    @Override
    public String getPresignedUrl(String fileKey, int expirationMinutes) {
        logger.info("Generating pre-signed URL: key={}, expiration={}min", fileKey, expirationMinutes);
        
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(storageConfig.getBucketName())
                    .key(fileKey)
                    .build();
            
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .getObjectRequest(getRequest)
                    .build();
            
            try (S3Presigner presigner = S3Presigner.create()) {
                PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
                String url = presignedRequest.url().toString();
                
                logger.info("✅ Pre-signed URL generated: expires in {}min", expirationMinutes);
                return url;
            }
            
        } catch (S3Exception e) {
            logger.error("❌ Failed to generate pre-signed URL: {}", e.getMessage());
            return null;
        }
    }
    
    @Override
    public boolean fileExists(String fileUrl) {
        String key = extractKeyFromUrl(fileUrl);
        
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(storageConfig.getBucketName())
                    .key(key)
                    .build();
            
            s3Client.headObject(headRequest);
            return true;
            
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            logger.error("Error checking file existence: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public long getFileSize(String fileUrl) {
        String key = extractKeyFromUrl(fileUrl);
        
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(storageConfig.getBucketName())
                    .key(key)
                    .build();
            
            HeadObjectResponse response = s3Client.headObject(headRequest);
            return response.contentLength();
            
        } catch (S3Exception e) {
            logger.error("Error getting file size: {}", e.getMessage());
            return -1;
        }
    }
    
    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty");
        }
        
        long maxSizeBytes = storageConfig.getMaxFileSizeMb() * 1024L * 1024L;
        if (file.getSize() > maxSizeBytes) {
            throw new IOException("File size exceeds maximum allowed: " + storageConfig.getMaxFileSizeMb() + " MB");
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
     * Generate unique file name to prevent conflicts
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
    
    /**
     * Build public URL for S3 object
     */
    private String buildPublicUrl(String key) {
        // Format: https://{bucket}.s3.{region}.amazonaws.com/{key}
        return String.format("https://%s.s3.%s.amazonaws.com/%s", 
                           storageConfig.getBucketName(), 
                           storageConfig.getRegion(), 
                           key);
    }
    
    /**
     * Extract S3 key from full URL
     */
    private String extractKeyFromUrl(String fileUrl) {
        // Extract key from URL like: https://bucket.s3.region.amazonaws.com/directory/file.jpg
        String bucketPrefix = storageConfig.getBucketName() + ".s3." + storageConfig.getRegion() + ".amazonaws.com/";
        int keyStart = fileUrl.indexOf(bucketPrefix);
        if (keyStart != -1) {
            return fileUrl.substring(keyStart + bucketPrefix.length());
        }
        // Fallback: assume it's already a key
        return fileUrl;
    }
}
