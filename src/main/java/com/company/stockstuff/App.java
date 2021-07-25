package com.company.stockstuff;

import java.util.TreeMap;

import com.company.stockstuff.dao.S3AccessDao;
import com.company.stockstuff.data.API;
import com.company.stockstuff.data.Candle;

import software.amazon.awssdk.regions.Region;

public class App {
	
	public static final TreeMap<String,Candle[]> history = new TreeMap<String,Candle[]>();
	
    public static void main(String[] args) {
    	S3AccessDao.setBucket("stock-data-bucket-ahcc");
    	S3AccessDao.setRegion(Region.US_EAST_2);
    	
    	String objectKey = "test3.txt";
    	String objectPath = "C:/Users/ChristopherCheng/Desktop/aws_xps/test2.txt";

        String result = S3AccessDao.putS3Object(objectKey,objectPath);
        System.out.println("Tag information: "+result);
        
        /*
        new Thread(()->{
        	collectData();
        });
         */
    }
    
    public static void collectData() {
    	
		long targetStartTime = Common.getTimeMillis(); // the time when we next want to sample market data
		long lastStartTime = targetStartTime;
		
		// where in the block are we
		int historyIndex = (int)(targetStartTime/1000);
		
		while(true) {
			long startTime = Common.getTimeMillis();
			
			// every hour, write all blocks to files
			if(startTime/Common.MILLIS_IN_HOUR>lastStartTime/Common.MILLIS_IN_HOUR) {
				int endHour = (int)Common.alignToHour(startTime);
				for(String assetName : history.keySet()) {
					try {
						Candle.IO.write(endHour,history.get(assetName),assetName);
					} catch(Exception e) {
						e.printStackTrace();
						System.err.println("sad");
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
					
					// add block if not already existing
					if(block==null) {
						block = new Candle[Common.SECOND_IN_HOUR];
						for(int i=0;i<block.length;i++) {
							block[i] = new Candle();
						}
						history.put(assetName,block);
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

