package jj;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatHelper {

	private static final ThreadLocal<SimpleDateFormat> BASIC = 
		new ThreadLocal<SimpleDateFormat>() {
			@Override
			protected SimpleDateFormat initialValue() {
				return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
			}
		};
	
	private static final ThreadLocal<SimpleDateFormat> URI =
		new ThreadLocal<SimpleDateFormat>() {
			@Override
			protected SimpleDateFormat initialValue() {
				return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			};
		};
		
	// 10/Oct/2000:13:55:36 -0700
	private static final ThreadLocal<SimpleDateFormat> ACCESS_LOG = 
		new ThreadLocal<SimpleDateFormat>() {
			@Override
			protected SimpleDateFormat initialValue() {
				return new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
			};
		};
	
	public static String nowInBasicFormat() {
		return BASIC.get().format(new Date());
	}
	
	public static String basicFormat(long timeInMillis) {
		return BASIC.get().format(new Date(timeInMillis));
	}
	
	public static String uriFormat(Date date) {
		return URI.get().format(date);
	}
	
	public static String nowInAccessLogFormat() {
		return ACCESS_LOG.get().format(new Date());
	}
}
