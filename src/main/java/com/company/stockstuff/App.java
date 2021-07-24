package com.company.stockstuff;

import com.company.stockstuff.dao.S3AccessDao;

import software.amazon.awssdk.regions.Region;

public class App 
{
    public static void main( String[] args )
    {
    	Region region = Region.US_EAST_2;
    	
    	S3AccessDao dao = new S3AccessDao(region);
    	    
    	String bucketName = "stock-data-bucket-ahcc";
    	
    	String objectKey = "test.txt";
    	String objectPath = "/Users/alanhuang/Desktop/EC2_files/test.txt";

        String result = dao.putS3Object(bucketName, objectKey, objectPath);
        System.out.println("Tag information: "+result);
        
    }
}

