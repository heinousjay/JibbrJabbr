package jj;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

public class Scratchpad {
	

	@Test
	public void prefixSelectionTest() {
		Document doc = Jsoup.parse("<html><head><title data-i18n='superoo' data-i18n-id='underoo'>balls</title></head></html>");
		
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

}
