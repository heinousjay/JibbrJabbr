package jj.http.server.servable.document;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.PropertyResourceBundle;

import jj.configuration.AppLocation;
import jj.http.server.servable.document.DocumentRequestProcessor;
import jj.http.server.servable.document.InlineMessagesDocumentFilter;
import jj.resource.IsThread;
import jj.resource.ResourceFinder;
import jj.resource.document.HtmlResource;
import jj.resource.property.PropertiesResource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InlineMessagesDocumentFilterTest {

	String name;
	@Mock ResourceFinder resourceFinder;
	@Mock PropertiesResource propertiesResource;
	PropertyResourceBundle bundle;
	@Mock DocumentRequestProcessor documentRequestProcessor;
	@Mock HtmlResource htmlResource;
	Document document;
	InlineMessagesDocumentFilter toTest;
	@Mock IsThread isThread;
	
	@Test
	public void test() throws IOException {

		// given
		name = "index";
		
		given(isThread.forIO()).willReturn(false);
		
		bundle = new PropertyResourceBundle(
			new StringReader(
				"hi=Why, hello there\ngoodbye=http://www.google.com/\ntitle=this is the secret"
			)
		);
		given(propertiesResource.properties()).willReturn(bundle);
		
		document = Jsoup.parse("<a id=\"test\" data-i18n-href=\"goodbye\" data-i18n-title=\"title\" data-i18n=\"hi\">HI MESSAGE HERE</a><p data-i18n=\"hi\">HI MESSAGE HERE ALSO</p>");
		given(documentRequestProcessor.document()).willReturn(document);
		
		given(documentRequestProcessor.baseName()).willReturn(name);
		
		given(resourceFinder.findResource(PropertiesResource.class, AppLocation.Base, name + ".properties")).willReturn(propertiesResource);
		
		toTest = new InlineMessagesDocumentFilter(resourceFinder, isThread);
		
		
		// when
		toTest.filter(documentRequestProcessor);
		
		// then
		assertThat(document.select("a").text(), is("Why, hello there"));
		assertThat(document.select("p").text(), is("Why, hello there"));
		assertThat(document.select("a").attr("href"), is("http://www.google.com/"));
		assertThat(document.select("a").attr("title"), is("this is the secret"));
	}

}
