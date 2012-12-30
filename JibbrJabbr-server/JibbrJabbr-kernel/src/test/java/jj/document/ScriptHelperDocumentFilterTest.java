package jj.document;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.net.URI;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import jj.jqmessage.JQueryMessage;
import jj.resource.ScriptResource;
import jj.resource.ScriptResourceType;
import jj.script.CurrentScriptContext;
import jj.script.ScriptBundle;
import jj.webbit.JJHttpRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScriptHelperDocumentFilterTest {
	
	URI uri; 
	String scriptUri;
	String socketUri;
	String webSocketUri;
	@Mock ScriptBundle scriptBundle;
	@Mock CurrentScriptContext context;
	@Mock ScriptResource scriptResource;
	@Mock JJHttpRequest httpRequest;
	Document document;
	@Mock DocumentRequest documentRequest;

	ScriptHelperDocumentFilter filter;
	
	@Before
	public void before() {
		uri = URI.create("http://localhost:8080/");
		scriptUri = "index/blahblahblah";
		socketUri = "socketURI";
		webSocketUri = uri.toString().replace("http", "ws") + socketUri;
		
		when(scriptBundle.toUri()).thenReturn(scriptUri);
		when(scriptBundle.toSocketUri()).thenReturn(socketUri);
		
		when(context.scriptBundle()).thenReturn(scriptBundle);
		when(context.httpRequest()).thenReturn(httpRequest);
		
		when(httpRequest.getStartupJQueryMessages())
			.thenReturn(Collections.<JQueryMessage>emptyList());

		document = Jsoup.parse("<html><head><title>what</title></head><body></body></html>");
		when(documentRequest.document()).thenReturn(document);

		
		filter = new ScriptHelperDocumentFilter(uri, context);
	}
	
	@Test
	public void testClientAndSharedScriptsAddedWhenPresent() {
		
		// given
		given(scriptBundle.clientScriptResource()).willReturn(scriptResource);
		given(scriptBundle.sharedScriptResource()).willReturn(scriptResource);
		given(scriptResource.type())
			.willReturn(ScriptResourceType.Shared)
			.willReturn(ScriptResourceType.Client);
		
		// when
		filter.filter(documentRequest);
		
		// then
		assertThat(document.select("script[type=text/javascript]").size(), is(4));
		assertThat(document.select("script[src=" + ScriptHelperDocumentFilter.JQUERY_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + ScriptHelperDocumentFilter.SOCKET_CONNECT_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + ScriptHelperDocumentFilter.SOCKET_CONNECT_URI + "]").attr("data-jj-socket-url"), is(webSocketUri));
		assertThat(document.select("script[src=/" + scriptUri + ".js]").size(), is(1));
		assertThat(document.select("script[src=/" + scriptUri + ".shared.js]").size(), is(1));
	}
	
	@Test
	public void testClientScriptIsAddedWhenPresentAndSharedScriptIsAbsent() {
		
		// given
		given(scriptBundle.clientScriptResource()).willReturn(scriptResource);
		given(scriptResource.type()).willReturn(ScriptResourceType.Client);
		
		// when
		filter.filter(documentRequest);
		
		// then
		assertThat(document.select("script[type=text/javascript]").size(), is(3));
		assertThat(document.select("script[src=" + ScriptHelperDocumentFilter.JQUERY_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + ScriptHelperDocumentFilter.SOCKET_CONNECT_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + ScriptHelperDocumentFilter.SOCKET_CONNECT_URI + "]").attr("data-jj-socket-url"), is(webSocketUri));
		assertThat(document.select("script[src=/" + scriptUri + ".js]").size(), is(1));
		assertThat(document.select("script[src=/" + scriptUri + ".shared.js]").size(), is(0));
	}
	
	@Test
	public void testClientScriptIsAddedWhenAbsentAndSharedScriptIsPresent() {
		
		// given
		given(scriptBundle.sharedScriptResource()).willReturn(scriptResource);
		given(scriptResource.type()).willReturn(ScriptResourceType.Shared);
		
		//when
		filter.filter(documentRequest);
		
		// then
		assertThat(document.select("script[type=text/javascript]").size(), is(3));
		assertThat(document.select("script[src=" + ScriptHelperDocumentFilter.JQUERY_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + ScriptHelperDocumentFilter.SOCKET_CONNECT_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + ScriptHelperDocumentFilter.SOCKET_CONNECT_URI + "]").attr("data-jj-socket-url"), is(webSocketUri));
		assertThat(document.select("script[src=/" + scriptUri + ".js]").size(), is(0));
		assertThat(document.select("script[src=/" + scriptUri + ".shared.js]").size(), is(1));
	}
	
	@Test
	public void testDefaultScriptsAreAddedWhenNothingIsPresent() {
		
		// given our default state
		
		// when
		filter.filter(documentRequest);
		
		//then
		assertThat(document.select("script[type=text/javascript]").size(), is(2));
		assertThat(document.select("script[src=" + ScriptHelperDocumentFilter.JQUERY_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + ScriptHelperDocumentFilter.SOCKET_CONNECT_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + ScriptHelperDocumentFilter.SOCKET_CONNECT_URI + "]").attr("data-jj-socket-url"), is(webSocketUri));
		assertThat(document.select("script[src=/" + scriptUri + ".js]").size(), is(0));
		assertThat(document.select("script[src=/" + scriptUri + ".shared.js]").size(), is(0));
	}
	
	@Test
	public void testMessagesAreAddedCorrectlyToTheScript() {
		
		// given
		List<JQueryMessage> messages = Arrays.asList(
			JQueryMessage.makeBind("a.fancybox", "click"),
			JQueryMessage.makeBind("a.fancyvideo", "click"),
			JQueryMessage.makeBind("#chatbox", "enter")
		);
		
		given(httpRequest.getStartupJQueryMessages()).willReturn(messages);
		filter = new ScriptHelperDocumentFilter(uri, context);
		
		// when 
		filter.filter(documentRequest);
		
		// then
		assertThat(document.select("#jj-connector-script").attr("data-jj-startup-messages"), is(messages.toString()));
	}

}
