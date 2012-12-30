package jj.document;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.PropertyResourceBundle;

import jj.JJExecutors;
import jj.resource.HtmlResource;
import jj.resource.PropertiesResource;
import jj.resource.ResourceFinder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InlineMessagesFilterTest {

	String baseName;
	@Mock ResourceFinder resourceFinder;
	@Mock PropertiesResource propertiesResource;
	PropertyResourceBundle bundle;
	@Mock DocumentRequest documentRequest;
	@Mock HtmlResource htmlResource;
	Document document;
	InlineMessagesFilter toTest;
	@Mock JJExecutors executors;
	
	@Before
	public void before() throws IOException {
		
		baseName = "index";
		
		given(executors.isIOThread()).willReturn(false);
		
		bundle = new PropertyResourceBundle(
			new StringReader(
				"hi=Why, hello there\ngoodbye=http://www.google.com/\ntitle=this is the secret"
			)
		);
		given(propertiesResource.properties()).willReturn(bundle);
		
		document = Jsoup.parse("<a id=\"test\" data-i18n-href=\"goodbye\" data-i18n-title=\"title\" data-i18n=\"hi\">HI MESSAGE HERE</a><p data-i18n=\"hi\">HI MESSAGE HERE ALSO</p>");
		given(documentRequest.document()).willReturn(document);
		
		given(documentRequest.htmlResource()).willReturn(htmlResource);
		
		given(htmlResource.baseName()).willReturn(baseName);
		
		given(resourceFinder.findResource(PropertiesResource.class, baseName)).willReturn(propertiesResource);
		
		toTest = new InlineMessagesFilter(resourceFinder, executors);
		
	}
	
	@Test
	public void test() {
		
		// given
		
		// when
		toTest.filter(documentRequest);
		
		// then
		assertThat(document.select("a").text(), is("Why, hello there"));
		assertThat(document.select("p").text(), is("Why, hello there"));
		assertThat(document.select("a").attr("href"), is("http://www.google.com/"));
		assertThat(document.select("a").attr("title"), is("this is the secret"));
	}

}
