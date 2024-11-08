package nus.iss.se.team9.recipe_service_team9.service;

//import com.example.s3.util.PresignUrlUtils;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
public class S3Service {
	/* Create a pre-signed URL to download an object in a subsequent GET request. */
	public String createPresignedGetUrl(String bucketName, String keyName) {
		String region = "ap-southeast-1";
		String accessKey = "AKIAVRUVU6A4H2YJ4RPV";
		String secretKey = "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";
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
}
