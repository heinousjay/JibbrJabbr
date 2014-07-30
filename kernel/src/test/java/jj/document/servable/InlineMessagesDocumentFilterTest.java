package jj.document.servable;

import static jj.configuration.resolution.AppLocation.Virtual;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.util.Locale;

import jj.document.DocumentScriptEnvironment;
import jj.document.servable.DocumentRequestProcessor;
import jj.document.servable.InlineMessagesDocumentFilter;
import jj.execution.CurrentTask;
import jj.messaging.MessagesResource;
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
	@Mock MessagesResource resource;
	
	@Mock DocumentRequestProcessor documentRequestProcessor;
	@Mock DocumentScriptEnvironment dse;
	
	Document document;
	
	InlineMessagesDocumentFilter toTest;
	
	@Mock CurrentTask currentTask;
	
	@Test
	public void test() throws IOException {

		// given
		name = "index";
		
		given(resource.containsKey("hi")).willReturn(true);
		given(resource.message("hi")).willReturn("Why, hello there");
		given(resource.containsKey("goodbye")).willReturn(true);
		given(resource.message("goodbye")).willReturn("http://www.google.com/");
		given(resource.containsKey("title")).willReturn(true);
		given(resource.message("title")).willReturn("this is the secret");
		
		document = Jsoup.parse(
			"<div data-i18n-class='missing-1' data-i18n='missing-2'></div><a id=\"test\" data-i18n-href=\"goodbye\" data-i18n-title=\"title\" data-i18n=\"hi\">HI MESSAGE HERE</a><p data-i18n=\"hi\">HI MESSAGE HERE ALSO</p>"
		);
		
		given(documentRequestProcessor.document()).willReturn(document);
		given(documentRequestProcessor.documentScriptEnvironment()).willReturn(dse);
		given(documentRequestProcessor.baseName()).willReturn(name);
		
		given(resourceFinder.findResource(MessagesResource.class, Virtual, name, Locale.US)).willReturn(resource);
		
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
		
		verify(resource).addDependent(dse);
	}

}
