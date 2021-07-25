package com.company.stockstuff.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.company.stockstuff.dao.S3AccessDao;

public class Candle {

	public double ask;
	public double bid;
	public int lag;
	
	public String toString() {
		return    "ask: "+ask+
				", bid: "+bid;
	}
	
	public static class IO {

		public static void write(Candle candle, DataOutputStream out) throws IOException {
			float avg = (float)(candle.ask+candle.bid);
			float spread = (float)(candle.ask-avg);
			out.writeFloat(avg);
			out.writeFloat(spread);
			out.writeInt(candle.lag);
		}
		
		public static void write(long timestamp, Candle[] block, String assetName) throws IOException {
			
			ByteArrayOutputStream data = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(data);
			for(int i=0;i<block.length;i++) {
				write(block[i],out);
			}
			out.close();

			String path = assetName+"-"+timestamp+".dat";
			S3AccessDao.putS3Object(path,data.toByteArray());
			data.close(); // not really needed but whatever
		}
		
		public static Candle read(DataInputStream in) throws IOException {
			Candle candle = new Candle();
			float avg = in.readFloat();
			float spread = in.readFloat();
			int lag = in.readInt();
			candle.ask = avg+spread;
			candle.bid = avg-spread;
			candle.lag = lag;
			return candle;
		}
		
		public static Candle[] read(long timestamp, String assetName) throws IOException {
			String path = assetName+"-"+timestamp+".dat";
			DataInputStream in = new DataInputStream(
					new ByteArrayInputStream(
					S3AccessDao.getS3Object(path)));
			Candle[] out = new Candle[3600];
			for(int i=0;i<out.length;i++) {
				out[i] = read(in);
			}
			return out;
		}
		
	}
	
}
