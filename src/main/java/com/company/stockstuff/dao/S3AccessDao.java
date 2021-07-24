package com.company.stockstuff.dao;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class S3AccessDao {
	
	private static S3Client s3;
	
	private static final String ACCESS_KEY = "AKIATBSTAPXV3EBKH3WJ";
	private static final String SECRET_KEY = "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";
	
	public S3AccessDao(Region region){
		AwsBasicCredentials credentials = AwsBasicCredentials.create(
			ACCESS_KEY,
			SECRET_KEY);
		s3 = S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(region)
            .build();
	}
	
	public String putS3Object(
			String bucketName,
	        String objectKey,
	        String objectPath) {
		
		try {
			Map<String, String> metadata = new HashMap<>();
			metadata.put("x-amz-meta-myVal","test");
			
			PutObjectRequest putOb = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(objectKey)
				.metadata(metadata)
				.build();
			
			PutObjectResponse response = s3.putObject(putOb,
				RequestBody.fromBytes(getObjectFile(objectPath)));
			
			return response.eTag();
		
		} catch (S3Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		return "";
	}
	
	
	private static byte[] getObjectFile(String filePath) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {
            File file = new File(filePath);
            bytesArray = new byte[(int) file.length()];
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bytesArray;
    }
	
}

