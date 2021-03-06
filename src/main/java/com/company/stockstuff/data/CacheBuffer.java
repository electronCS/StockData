package com.company.stockstuff.data;

import java.util.ArrayList;
import java.util.Date;

import com.company.stockstuff.App;
import com.company.stockstuff.Common;

public class CacheBuffer {

	public static class Entry {
		
		public String name;
		public Candle[] data;
		
		public Entry(String name, Candle[] data) {
			this.name = name;
			this.data = data;
		}
		
	}
	
	public ArrayList<Entry> cache = new ArrayList<Entry>();
	
	public Entry findEntry(String name) {
		for(Entry entry : cache) {
		if(entry.name.equals(name)) {
			return entry;
		}
		}
		return null;
	}
	
	public int getAlignedTimestamp(Date date) { // next hour
		return (int)Common.alignToHour(date.getTime())+1;
	}
	
	public int getSubHourTimestamp(Date date) { // in seconds
		return (int)((date.getTime()%Common.MILLIS_IN_HOUR)/1000);
	}
	
	public Candle[] getRange(String assetName, Date head, Date tail) {
		
		int head_timestamp = getAlignedTimestamp(head);
		int tail_timestamp = getAlignedTimestamp(tail);
		
		ArrayList<Candle> out = new ArrayList<Candle>();
		
		for(int timestamp=head_timestamp;timestamp<=tail_timestamp;timestamp+=3600000) {
			String name = assetName+"-"+timestamp;
			try {
				Candle[] data = null;
				
				if(timestamp<getAlignedTimestamp(new Date())) {
					// if data was in past hours, collect it from filesystem or cache
					Entry entry = findEntry(name);
					if(entry==null) {
						entry = new Entry(name,Candle.IO.read(timestamp,assetName));
					}
					data = entry.data;
				} else {
					// otherwise grab data from App.history
					data = App.history.get(assetName);
				}
				
				int data_head = 0;
				int data_tail = data.length;
				if(timestamp==head_timestamp) {
					data_head = getSubHourTimestamp(head);
				}
				if(timestamp==tail_timestamp) {
					data_tail = getSubHourTimestamp(tail);
				}
				for(int i=data_head;i<data_tail;i++) {
					out.add(data[i]);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return out.toArray(new Candle[out.size()]);
	}
	
}
