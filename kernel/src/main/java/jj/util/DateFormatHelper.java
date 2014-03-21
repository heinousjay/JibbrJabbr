package jj.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public enum DateFormatHelper {

	; // no instances
	
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
		
		
	private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
	/*
	 * need to support all three formats on incoming headers, as detailed:
	 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3
	 * 
		Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123
		Sunday, 06-Nov-94 08:49:37 GMT ; RFC 850, obsoleted by RFC 1036
		Sun Nov  6 08:49:37 1994       ; ANSI C's asctime() format
	*/
	private static final ThreadLocal<DateFormat> RFC_1123 = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
			format.setLenient(false);
			return format;
		}
		
		@Override
		public DateFormat get() {
			DateFormat format = super.get();
			format.setTimeZone(GMT);
			return format;
		}
	};
	
	private static final ThreadLocal<DateFormat> RFC_850 = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			DateFormat format = new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss zzz", Locale.ENGLISH);
			format.setLenient(false);
			return format;
		}
		
		@Override
		public DateFormat get() {
			DateFormat format = super.get();
			format.setTimeZone(GMT);
			return format;
		}
	};

	private static final ThreadLocal<DateFormat> ASCTIME_2_DIGIT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH);
			format.setLenient(false);
			return format;
		}
		
		@Override
		public DateFormat get() {
			DateFormat format = super.get();
			format.setTimeZone(GMT);
			return format;
		}
	};

	private static final ThreadLocal<DateFormat> ASCTIME_1_DIGIT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			DateFormat format = new SimpleDateFormat("EEE MMM  d HH:mm:ss yyyy", Locale.ENGLISH);
			format.setLenient(false);
			return format;
		}
		
		@Override
		public DateFormat get() {
			DateFormat format = super.get();
			format.setTimeZone(GMT);
			return format;
		}
	};
	
	public static String nowInBasicFormat() {
		return BASIC.get().format(new Date());
	}
	
	public static String basicFormat(final long timeInMillis) {
		return BASIC.get().format(new Date(timeInMillis));
	}
	
	public static String uriFormat(final Date date) {
		assert date != null : "date cannot be null";
		return URI.get().format(date);
	}
	
	public static String nowInAccessLogFormat() {
		return ACCESS_LOG.get().format(new Date());
	}
	
	public static String nowInHeaderFormat() {
		return headerFormat(new Date());
	}
	
	public static String headerFormat(final Date date) {
		assert date != null : "date cannot be null";
		return RFC_1123.get().format(date);
	}
	
	public static Date headerDate(final String headerValue) {
		assert headerValue != null : "header value cannot be null";
		try {
			return RFC_1123.get().parse(headerValue);
		} catch (ParseException e) {
			try {
				return RFC_850.get().parse(headerValue);
			} catch (ParseException e1) {
				try {
					return ASCTIME_2_DIGIT.get().parse(headerValue);
				} catch (ParseException e2) {
					try {
						return ASCTIME_1_DIGIT.get().parse(headerValue);
					} catch (ParseException e3) {
						return null;
					}
				}
			}
		}
	}
}
