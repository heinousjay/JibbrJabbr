package jj;

import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Ignore;
import org.junit.Test;

public class Scratchpad {
	

	final String html = "<html><head><title data-i18n='superoo' data-i18n-id='underoo'>balls</title></head></html>";

	@Test
	public void testNormalizeString() {
		
		System.out.println("|" + StringUtil.normaliseWhitespace("			   something   	\n\r \n\r		") + "|");
		
	}
	
	@Ignore @Test
	public void prefixSelectionTest() {
		Document doc = Jsoup.parse(html);
		
		final String KEY = "data-i18n-";
		for (final Element el : doc.select("[^" + KEY + "]")) {
			// need to go through all the attributes looking for keys?
			for (final Attribute attr : el.attributes()) {
				if (attr.getKey().startsWith(KEY)) {
					String newAttr = attr.getKey().substring(KEY.length());
					el.attr(newAttr, attr.getValue())
						.removeAttr(attr.getKey());
				}
			}
		}
		
		System.out.println(doc);
	}
	
	
	@SuppressWarnings("unused")
	@Test
	public void jsoupCoarseProfile() {
		
		// 15,000 iterations so it JITs stuff
		for (int i = 0; i < 45000; ++i) {
			Jsoup.parse(html).clone();
		}
		
		long start = System.nanoTime();
		
		for (int i = 0; i < 15000; ++i) {
			Document doc = Jsoup.parse(html);
		}
		
		System.out.println(TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS));
		
		Document doc = Jsoup.parse(html);
		
		start = System.nanoTime();
		
		
		for (int i = 0; i < 15000; ++i) {
			Document doc2 = doc.clone();
		}
		
		System.out.println(TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS));
		
		start = System.nanoTime();
		
		for (int i = 0; i < 15000; ++i) {
			Document doc3 = Jsoup.parse(html);
		}
		
		System.out.println(TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS));
		
		Document doc4 = Jsoup.parse(html);
		
		start = System.nanoTime();
		
		
		for (int i = 0; i < 15000; ++i) {
			Document doc5 = doc4.clone();
		}
		
		System.out.println(TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS));
	}

}
