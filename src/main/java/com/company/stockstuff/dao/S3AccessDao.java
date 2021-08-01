package com.company.stockstuff.dao;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class S3AccessDao {

	private static String ACCESS_KEY;
	private static String SECRET_KEY;
	
	private static S3Client s3;
	private static String bucketName;
	
	public static void loadKey(String path) {
		JSONObject keyinfo = new JSONObject(new String(getObjectFile(path)));
		ACCESS_KEY = keyinfo.getString("access_key");
		SECRET_KEY = keyinfo.getString("secret_key");
	}
	
	public static void setBucket(String name) {
		bucketName = name;
	}
	
	public static void setRegion(Region region) {
		AwsBasicCredentials credentials = AwsBasicCredentials.create(
			ACCESS_KEY,
			SECRET_KEY);
		s3 = S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(region)
            .build();
	}
	
	public static String putS3Object(
	        String objectKey,
	        byte[] data) {
		
		try {
			/*
			Map<String, String> metadata = new HashMap<>();
			metadata.put("x-amz-meta-myVal","test");
			*/
			PutObjectRequest putOb = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(objectKey)
				//.metadata(metadata)
				.build();
			
			PutObjectResponse response = s3.putObject(putOb,RequestBody.fromBytes(data));
			
			return response.eTag();
		
		} catch (S3Exception e) {
			//System.err.println(e.getMessage());
		}
		
		return null;
	}
	
	public static String putS3Object(
	        String objectKey,
	        String objectPath) {
		return putS3Object(objectKey,getObjectFile(objectPath));
	}
	
	public static byte[] getS3Object(String objectKey) {
		
		try {
			
			GetObjectRequest putOb = GetObjectRequest.builder()
				.bucket(bucketName)
				.key(objectKey)
				.build();
			
			BufferedInputStream in = new BufferedInputStream(s3.getObject(putOb));
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int bytes_read;
			while((bytes_read=in.read(buffer,0,buffer.length))!=-1) {
				out.write(buffer,0,bytes_read);
			}
			return out.toByteArray();
			
		} catch (Exception e) {
			//System.err.println(e.getMessage());
		}
		
		return null;
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

