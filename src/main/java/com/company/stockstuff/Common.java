package com.company.stockstuff;

import java.util.Date;

public class Common {
	public static final int SECOND_IN_HOUR = 3600;
	public static final int MILLIS_IN_HOUR = SECOND_IN_HOUR*1000;
	
	public static long getTimeMillis() {
		return new Date().getTime();
	}
	
	public static long alignToHour(long n) { // assuming n is in milliseconds
		return ((n/MILLIS_IN_HOUR)*MILLIS_IN_HOUR);
	}

}
