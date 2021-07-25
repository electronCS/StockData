package com.company.stockstuff.data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class API {

	public String call(String url) {
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
					new URL(url).openStream()));
			StringBuilder out = new StringBuilder();
			String line;
			while((line=in.readLine())!=null) {
				out.append(line);
				out.append("\n");
			}
			in.close();
			return out.toString();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public abstract TreeMap<String,Candle> getCurrentCandles();
	
	public static final API Crypto = new API(){
		public TreeMap<String,Candle> getCurrentCandles() {
			TreeMap<String,Candle> out = new TreeMap<String,Candle>();
			String response = call("https://api.binance.us/api/v3/ticker/bookTicker");
			try {
				JSONArray data = new JSONArray(response);
				for(int i=0;i<data.length();i++) {
					JSONObject entry = data.getJSONObject(i);
					String assetName = entry.getString("symbol");
					Candle candle = new Candle();
					candle.bid = entry.getDouble("bidPrice");
					candle.ask = entry.getDouble("askPrice");
					out.put(assetName,candle);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			return out;
		}
	};
	
	public static final API Stocks = new API(){
		public TreeMap<String,Candle> getCurrentCandles() {
			TreeMap<String,Candle> out = new TreeMap<String,Candle>();
			/*
			String response = call("https://data.alpaca.markets/v1");
			try {
				JSONArray data = JSONArray.parse(response);
				for(int i=0;i<data.size();i++) {
					JSONObject entry = data.getJSONObject(i);
					String assetName = entry.getString("symbol");
					Candle candle = new Candle();
					candle.bid = entry.getDouble("bidPrice");
					candle.ask = entry.getDouble("askPrice");
					out.put(assetName,candle);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			*/
			return out;
		}
	};
	
}
