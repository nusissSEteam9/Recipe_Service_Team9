//package nus.iss.se.team9.recipe_service_team9.model;
//
//import org.springframework.cloud.aws.context.config.annotation.EnableContextRegion;
//import org.springframework.cloud.aws.context.config.annotation.EnableContextCredentials;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
//import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.s3.S3Client;
//
//@Configuration
//@EnableContextRegion
//@EnableContextCredentials
//public class AmazonS3Config {
//
//    @Bean
//    public S3Client s3Client() {
//        AwsBasicCredentials credentials = AwsBasicCredentials.create(
//                System.getProperty("cloud.aws.credentials.access-key"),
//                System.getProperty("cloud.aws.credentials.secret-key"));
//
//        return S3Client.builder()
//                .credentialsProvider(StaticCredentialsProvider.create(credentials))
//                .region(Region.of(System.getProperty("cloud.aws.region.static")))
//                .build();
//    }
//}
