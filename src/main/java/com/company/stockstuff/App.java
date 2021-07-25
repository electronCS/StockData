package com.company.stockstuff;

import com.company.stockstuff.dao.S3AccessDao;

import software.amazon.awssdk.regions.Region;

public class App {
	
    public static void main(String[] args) {
    	S3AccessDao.setBucket("stock-data-bucket-ahcc");
    	S3AccessDao.setRegion(Region.US_EAST_2);
    	
    	String objectKey = "test3.txt";
    	String objectPath = "C:/Users/ChristopherCheng/Desktop/aws_xps/test2.txt";

        String result = S3AccessDao.putS3Object(objectKey,objectPath);
        System.out.println("Tag information: "+result);
    }
    
}

