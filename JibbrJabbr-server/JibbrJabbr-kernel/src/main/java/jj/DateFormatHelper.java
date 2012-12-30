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
	
	public static String nowInBasicFormat() {
		return BASIC.get().format(new Date());
	}
	
	public static String basicFormat(long timeInMillis) {
		return BASIC.get().format(new Date(timeInMillis));
	}
}
