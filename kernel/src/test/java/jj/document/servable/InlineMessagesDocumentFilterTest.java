package jj.document.servable;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.util.HashMap;

import jj.configuration.resolution.AppLocation;
import jj.document.HtmlResource;
import jj.document.servable.DocumentRequestProcessor;
import jj.document.servable.InlineMessagesDocumentFilter;
import jj.execution.CurrentTask;
import jj.messaging.PropertiesResource;
import jj.resource.ResourceFinder;

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
	HashMap<String, String> bundle;
	@Mock DocumentRequestProcessor documentRequestProcessor;
	@Mock HtmlResource htmlResource;
	Document document;
	InlineMessagesDocumentFilter toTest;
	@Mock CurrentTask currentTask;
	
	@Test
	public void test() throws IOException {

		// given
		name = "index";
		
		bundle = new HashMap<>();
		bundle.put("hi", "Why, hello there");
		bundle.put("goodbye", "http://www.google.com/");
		bundle.put("title", "this is the secret");
		
		given(propertiesResource.properties()).willReturn(bundle);
		
		document = Jsoup.parse(
			"<div data-i18n-class='missing-1' data-i18n='missing-2'></div><a id=\"test\" data-i18n-href=\"goodbye\" data-i18n-title=\"title\" data-i18n=\"hi\">HI MESSAGE HERE</a><p data-i18n=\"hi\">HI MESSAGE HERE ALSO</p>");
		given(documentRequestProcessor.document()).willReturn(document);
		
		given(documentRequestProcessor.baseName()).willReturn(name);
		
		given(resourceFinder.findResource(PropertiesResource.class, AppLocation.Base, name + ".properties")).willReturn(propertiesResource);
		
		toTest = new InlineMessagesDocumentFilter(resourceFinder, currentTask);
		
		
		// when
		toTest.filter(documentRequestProcessor);
		
		// then
		assertThat(document.select("div").text(), is(String.format(InlineMessagesDocumentFilter.MISSING_KEY, "missing-2")));
		assertThat(document.select("div").attr("class"), is(String.format(InlineMessagesDocumentFilter.MISSING_KEY, "missing-1")));
		assertThat(document.select("a").text(), is("Why, hello there"));
		assertThat(document.select("p").text(), is("Why, hello there"));
		assertThat(document.select("a").attr("href"), is("http://www.google.com/"));
		assertThat(document.select("a").attr("title"), is("this is the secret"));
	}

}
