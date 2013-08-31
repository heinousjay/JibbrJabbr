package jj;

import org.jsoup.helper.StringUtil;
import org.junit.Ignore;
import org.junit.Test;

public class Scratchpad {
	

	final String html = "<html><head><title data-i18n='superoo' data-i18n-id='underoo'>balls</title></head></html>";

	@Ignore @Test
	public void testNormalizeString() {
		
		System.out.println("|" + StringUtil.normaliseWhitespace("			   something   	\n\r \n\r		") + "|");
		
	}
	
	@Test
	public void guiceFun() {
		
		
	}

}
