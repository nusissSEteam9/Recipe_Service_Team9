package nus.iss.se.team9.recipe_service_team9.service;

//import com.example.s3.util.PresignUrlUtils;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class S3Service {
    private final String region = "ap-southeast-1";
    private final String accessKey = "AKIAVRUVU6A4H2YJ4RPV";
    private final String secretKey = "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";
    private final String bucketName = "healthy-recipe-images";

    /* Create a pre-signed URL to download an object in a subsequent GET request. */
    public String createPresignedGetUrl(String keyName) {
        try (S3Presigner presigner = S3Presigner.builder()
                                                .region(Region.of(region))
                                                .credentialsProvider(StaticCredentialsProvider.create(
                                                        AwsBasicCredentials.create(accessKey, secretKey)))
                                                .build()) {

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                                                .bucket(bucketName)
                                                                .key(keyName)
                                                                .acl("public-read")
                                                                .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                                                                            .signatureDuration(Duration.ofMinutes(
                                                                                    10))  // URL validity period
                                                                            .putObjectRequest(putObjectRequest)
                                                                            .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
            return presignedRequest.url()
                                   .toExternalForm();
        }
    }

    public List<String> ListAllBuckets() {
        S3Client s3Client = S3Client.builder()
                                    .region(Region.of(region))
                                    .credentialsProvider(StaticCredentialsProvider.create(
                                            AwsBasicCredentials.create(accessKey, secretKey)))
                                    .build();
        ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
        List<String> bucketNames = listBucketsResponse.buckets()
                                                      .stream()
                                                      .map(Bucket::name)
                                                      .toList();
        s3Client.close();
        return bucketNames;
    }

    public List<String> ListAllObjects() {
        S3Client s3Client = S3Client.builder()
                                    .region(Region.of(region))
                                    .credentialsProvider(StaticCredentialsProvider.create(
                                            AwsBasicCredentials.create(accessKey, secretKey)))
                                    .build();
        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                                                                      .bucket(bucketName)
                                                                      .build();
        ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);
        List<String> Urls = new ArrayList<>();
        for (S3Object s3Object : listObjectsResponse.contents()) {
            String key = s3Object.key();
            String url = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
            Urls.add(url);
        }
        s3Client.close();
        return Urls;
    }

    public void deleteObject(String keyName) throws Exception {
        try {
            S3Client s3Client = S3Client.builder()
                                        .region(Region.of(region))
                                        .credentialsProvider(StaticCredentialsProvider.create(
                                                AwsBasicCredentials.create(accessKey, secretKey)))
                                        .build();
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                                                                         .bucket(bucketName)
                                                                         .key(keyName)
                                                                         .build();
            s3Client.deleteObject(deleteObjectRequest);
            s3Client.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void uploadObject(MultipartFile file, String keyName) throws IOException {

        Path tempFile = Files.createTempFile("temp-", keyName);
        Files.copy(file.getInputStream(), tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        S3Client s3Client = S3Client.builder()
                                    .region(Region.of(region))
                                    .credentialsProvider(StaticCredentialsProvider.create(
                                            AwsBasicCredentials.create(accessKey, secretKey)))
                                    .build();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                                            .bucket(bucketName)
                                                            .key(keyName)
                                                            .build();
        PutObjectResponse response = s3Client.putObject(putObjectRequest, tempFile);
        System.out.println("Upload successful. ETag: " + response.eTag());
        Files.delete(tempFile);
    }

    public String getObjectUrl(String keyName) {
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + keyName;
    }

}
