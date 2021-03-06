package com.company.stockstuff;

import java.util.Scanner;
import java.util.TreeMap;

import com.company.stockstuff.dao.S3AccessDao;
import com.company.stockstuff.data.API;
import com.company.stockstuff.data.Candle;

import software.amazon.awssdk.regions.Region;

public class App {
	
	public static final TreeMap<String,Candle[]> history = new TreeMap<String,Candle[]>();
	
	public static int historyIndex = 0;
	
    public static void main(String[] args) {
    	
    	System.out.println("initializing dao...");
    	String keypath = System.getProperty("user.dir")+"/key.txt";
    	S3AccessDao.loadKey(keypath);
    	S3AccessDao.setBucket("stock-data-bucket-ahcc");
    	S3AccessDao.setRegion(Region.US_EAST_2);
    	
    	/*String objectKey = "test4.txt";
    	String objectPath = "C:/Users/ChristopherCheng/Desktop/aws_xps/test2.txt";

        String result = S3AccessDao.putS3Object(objectKey,objectPath);
        System.out.println("Tag information: "+result);
        */
    	new Thread(()->{
    		collectData();
    	}).start();
    	
    	final Scanner scanner = new Scanner(System.in);
    	while(true) {
    		String line = scanner.nextLine();
    		if("second".indexOf(line.trim())==0) {
    			System.out.println(historyIndex+"/3600 seconds");
    		} else if("hour".indexOf(line.trim())==0) {
    			int hour = (int)Common.alignToHour(Common.getTimeMillis());
    			System.out.println("data collected is for "+(hour-Common.MILLIS_IN_HOUR)+" to "+(hour-1)+" (epoch ms)");
    		} else if(line.equals("exit")) {
    			scanner.close();
    			System.exit(1);
    		}
    	}
    	
    }
    
    public static void collectData() {
    	
		long targetStartTime = Common.getTimeMillis(); // the time when we next want to sample market data
		long lastStartTime = targetStartTime;
		
		// where in the block are we
		historyIndex = ((int)(targetStartTime/1000))%Common.SECOND_IN_HOUR;
		
		System.out.println("starting data collection...");
		
		while(true) {
			long startTime = Common.getTimeMillis();
			
			// every hour, write all blocks to files
			if(startTime/Common.MILLIS_IN_HOUR>lastStartTime/Common.MILLIS_IN_HOUR) {
				int endHour = (int)Common.alignToHour(startTime);
				System.out.println("saved data to bucket for hour "+endHour);
				for(String assetName : history.keySet()) {
					try {
						Candle.IO.write(endHour,history.get(assetName),assetName);
					} catch(Exception e) {
						e.printStackTrace();
						System.err.println("failed to save data for asset: "+assetName);
					}
				}
				historyIndex = 0;
			}
			
			try {
				
				// collect crypto and stock data
				TreeMap<String,Candle> candles = new TreeMap<String,Candle>();
				candles.putAll(API.Crypto.getCurrentCandles());
				candles.putAll(API.Stocks.getCurrentCandles());
				
				// collect some timing/lag info (stored in each sample)
				long endTime = Common.getTimeMillis();
				int lag = (int)(endTime-startTime);
				
				// add all samples to the latest hour-aligned block for their corresponding assets
				for(String assetName : candles.keySet()) {
					Candle[] block = history.get(assetName);
					
					// add block (in ram) if not already existing
					if(block==null) {
						System.out.println("tracking new asset "+assetName+"...");
						
						// check if block already was partially filled
						block = Candle.IO.read(Common.alignToHour(endTime),assetName);
						if(block==null) {
							block = new Candle[Common.SECOND_IN_HOUR];
							for(int i=0;i<block.length;i++) {
								block[i] = new Candle();
							}
						}
						history.put(assetName,block);
						/*
						block = new Candle[Common.SECOND_IN_HOUR];
						for(int i=0;i<block.length;i++) {
							block[i] = new Candle();
						}
						history.put(assetName,block);
						*/
					}
					
					// put the candle in the block
					Candle candle = candles.get(assetName);
					candle.lag = lag;
					block[historyIndex] = candle;
					
				}
				
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			lastStartTime = startTime;
			
			targetStartTime += 1000;
			historyIndex++;
			
			// try to align api calls with each second
			try {
				long endTime = Common.getTimeMillis();
			
				int delay = (int)(targetStartTime-endTime);
				if(delay>0) {
					Thread.sleep(delay);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			
		}
    }
    
}

