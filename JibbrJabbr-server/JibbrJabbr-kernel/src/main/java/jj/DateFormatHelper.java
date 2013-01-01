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
	
	public static String nowInBasicFormat() {
		return BASIC.get().format(new Date());
	}
	
	public static String basicFormat(long timeInMillis) {
		return BASIC.get().format(new Date(timeInMillis));
	}
	
	public static String uriFormat(Date date) {
		return URI.get().format(date);
	}
}
