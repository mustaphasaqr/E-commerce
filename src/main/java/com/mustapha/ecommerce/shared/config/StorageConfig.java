package com.mustapha.ecommerce.shared.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

/**
 * AWS S3 File Storage Configuration
 * 
 * Setup Instructions for FREE Tier:
 * 1. Create AWS Account: https://aws.amazon.com/free
 * 2. Free Tier includes:
 *    - 5 GB Storage
 *    - 20,000 GET requests per month
 *    - 2,000 PUT requests per month
 *    - 15 GB Data Transfer out per month
 * 
 * 3. Create S3 Bucket:
 *    - Go to AWS Console → S3
 *    - Create bucket (e.g., "ecommerce-product-images")
 *    - Choose region (e.g., eu-central-1 for Europe, us-east-1 for USA)
 *    - Enable versioning (optional)
 *    - Set bucket policy for public read access (for product images)
 * 
 * 4. Create IAM User:
 *    - Go to IAM → Users → Add user
 *    - Attach policy: AmazonS3FullAccess (or custom policy)
 *    - Create access key
 * 
 * 5. Set Environment Variables:
 *    - AWS_ACCESS_KEY_ID=your_access_key
 *    - AWS_SECRET_ACCESS_KEY=your_secret_key
 *    - AWS_S3_BUCKET_NAME=your_bucket_name
 *    - AWS_REGION=eu-central-1 (or your region)
 * 
 * Security Best Practices:
 * - Never commit credentials to Git
 * - Use AWS IAM roles in production (EC2, ECS)
 * - Enable bucket versioning for backup
 * - Set lifecycle policies to delete old files
 * - Use CloudFront CDN for better performance (optional)
 * 
 * Supported File Types:
 * - Images: jpg, jpeg, png, gif, webp
 * - Max file size: 5 MB per image
 * - Naming convention: {productId}/{timestamp}-{originalFilename}
 */
@Configuration
public class StorageConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(StorageConfig.class);
    
    @Value("${aws.access-key-id:#{null}}")
    private String awsAccessKeyId;
    
    @Value("${aws.secret-access-key:#{null}}")
    private String awsSecretAccessKey;
    
    @Value("${aws.s3.bucket-name:#{null}}")
    private String bucketName;
    
    @Value("${aws.s3.region:eu-central-1}")
    private String region;
    
    @Value("${storage.enabled:true}")
    private boolean storageEnabled;
    
    @Value("${storage.max-file-size-mb:5}")
    private int maxFileSizeMb;
    
    @PostConstruct
    public void logConfiguration() {
        if (!isConfigured()) {
            logger.warn("⚠️ AWS S3 credentials not configured. File storage will use LOCAL mode.");
            logger.warn("   For production, set environment variables:");
            logger.warn("   - AWS_ACCESS_KEY_ID=your_access_key");
            logger.warn("   - AWS_SECRET_ACCESS_KEY=your_secret_key");
            logger.warn("   - AWS_S3_BUCKET_NAME=your_bucket_name");
            logger.warn("   - AWS_REGION=eu-central-1");
        }
    }
    
    @Bean
    @ConditionalOnExpression("#{environment.getProperty('aws.access-key-id') != null && !environment.getProperty('aws.access-key-id').isEmpty() && environment.getProperty('aws.secret-access-key') != null && !environment.getProperty('aws.secret-access-key').isEmpty() && environment.getProperty('aws.s3.bucket-name') != null && !environment.getProperty('aws.s3.bucket-name').isEmpty()}")
    public S3Client s3Client() {
        logger.info("🔧 Configuring AWS S3 client...");
        
        try {
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
                awsAccessKeyId,
                awsSecretAccessKey
            );
            
            AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(awsCredentials);
            
            S3ClientBuilder builder = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(credentialsProvider);
            
            S3Client client = builder.build();
            
            logger.info("✅ AWS S3 storage configured");
            logger.info("   Region: {}", region);
            logger.info("   Bucket: {}", bucketName);
            logger.info("   Max file size: {} MB", maxFileSizeMb);
            logger.info("   Access Key ends with: {}", 
                       awsAccessKeyId.substring(Math.max(0, awsAccessKeyId.length() - 4)));
            
            return client;
            
        } catch (Exception e) {
            logger.error("❌ Failed to configure AWS S3 client: {}", e.getMessage());
            throw new RuntimeException("Failed to configure AWS S3 client", e);
        }
    }
    
    public String getBucketName() {
        return bucketName;
    }
    
    public String getRegion() {
        return region;
    }
    
    public boolean isStorageEnabled() {
        return storageEnabled;
    }
    
    public int getMaxFileSizeMb() {
        return maxFileSizeMb;
    }
    
    public boolean isConfigured() {
        return awsAccessKeyId != null && !awsAccessKeyId.isBlank() &&
               awsSecretAccessKey != null && !awsSecretAccessKey.isBlank() &&
               bucketName != null && !bucketName.isBlank();
    }
}
