package com.company.stockstuff;

import java.util.Date;

public class Common {

	public static long getTimeMillis() {
		return new Date().getTime();
	}
	
	public static long alignToHour(long n) {
		return ((n/3600000)*3600000);
	}

}
