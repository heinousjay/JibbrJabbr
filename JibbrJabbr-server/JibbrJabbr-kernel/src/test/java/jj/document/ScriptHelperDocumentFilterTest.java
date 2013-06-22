package jj.document;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.net.URI;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import jj.configuration.Configuration;
import jj.jqmessage.JQueryMessage;
import jj.resource.AssetResource;
import jj.resource.ResourceFinder;
import jj.resource.ScriptResource;
import jj.resource.ScriptResourceType;
import jj.script.CurrentScriptContext;
import jj.script.AssociatedScriptBundle;
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
	
	static final String JQUERY_SHA = "aeb0805239eb1c1158894f8d25b4272ee6e67e44";
	static final String JQUERY_URI = "/" + JQUERY_SHA + "/" + ScriptHelperDocumentFilter.JQUERY_JS;
	static final String JJ_SHA = "be03b9352e1e254cae9a58cff2b20e0c8d513e47";
	static final String JJ_URI = "/" + JJ_SHA + "/" + ScriptHelperDocumentFilter.JJ_JS;
	
	String scriptUri;
	String socketUri;
	String webSocketUri;
	@Mock Configuration configuration;
	@Mock AssociatedScriptBundle associatedScriptBundle;
	@Mock CurrentScriptContext context;
	@Mock ScriptResource scriptResource;
	@Mock JJHttpRequest httpRequest;
	@Mock ResourceFinder resourceFinder;
	@Mock AssetResource jqueryJs;
	@Mock AssetResource jjJs;
	Document document;
	@Mock DocumentRequest documentRequest;

	ScriptHelperDocumentFilter filter;
	
	@Before
	public void before() {
		
		when(configuration.baseUri()).thenReturn(URI.create("http://localhost:8080/"));
		
		scriptUri = "index/blahblahblah";
		socketUri = "socketURI";
		webSocketUri = configuration.baseUri().toString().replace("http", "ws") + socketUri;
		
		when(associatedScriptBundle.toUri()).thenReturn(scriptUri);
		when(associatedScriptBundle.toSocketUri()).thenReturn(socketUri);
		
		when(context.associatedScriptBundle()).thenReturn(associatedScriptBundle);
		when(context.httpRequest()).thenReturn(httpRequest);
		
		when(httpRequest.startupJQueryMessages()).thenReturn(Collections.<JQueryMessage>emptyList());
		when(httpRequest.host()).thenReturn("localhost:8080");

		document = Jsoup.parse("<html><head><title>what</title></head><body></body></html>");
		when(documentRequest.document()).thenReturn(document);
		when(documentRequest.httpRequest()).thenReturn(httpRequest);
		
		when(resourceFinder.findResource(AssetResource.class, ScriptHelperDocumentFilter.JQUERY_JS))
			.thenReturn(jqueryJs);
		when(resourceFinder.findResource(AssetResource.class, ScriptHelperDocumentFilter.JJ_JS))
			.thenReturn(jjJs);
		
		when(jqueryJs.sha1()).thenReturn(JQUERY_SHA);
		when(jqueryJs.uri()).thenReturn(JQUERY_URI);
		
		when(jjJs.sha1()).thenReturn(JJ_SHA);
		when(jjJs.uri()).thenReturn(JJ_URI);
		
		filter = new ScriptHelperDocumentFilter(configuration, context, resourceFinder);
	}
	
	@Test
	public void testClientAndSharedScriptsAddedWhenPresent() {
		
		// given
		given(associatedScriptBundle.clientScriptResource()).willReturn(scriptResource);
		given(associatedScriptBundle.sharedScriptResource()).willReturn(scriptResource);
		given(scriptResource.type())
			.willReturn(ScriptResourceType.Shared)
			.willReturn(ScriptResourceType.Client);
		
		// when
		filter.filter(documentRequest);
		
		// then
		assertThat(document.select("script[type=text/javascript]").size(), is(4));
		assertThat(document.select("script[src=" + JQUERY_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + JJ_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + JJ_URI + "]").attr("data-jj-socket-url"), is(webSocketUri));
		assertThat(document.select("script[src=/" + scriptUri + ".js]").size(), is(1));
		assertThat(document.select("script[src=/" + scriptUri + ".shared.js]").size(), is(1));
	}
	
	@Test
	public void testClientScriptIsAddedWhenPresentAndSharedScriptIsAbsent() {
		
		// given
		given(associatedScriptBundle.clientScriptResource()).willReturn(scriptResource);
		given(scriptResource.type()).willReturn(ScriptResourceType.Client);
		
		// when
		filter.filter(documentRequest);
		
		// then
		assertThat(document.select("script[type=text/javascript]").size(), is(3));
		assertThat(document.select("script[src=" + JQUERY_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + JJ_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + JJ_URI + "]").attr("data-jj-socket-url"), is(webSocketUri));
		assertThat(document.select("script[src=/" + scriptUri + ".js]").size(), is(1));
		assertThat(document.select("script[src=/" + scriptUri + ".shared.js]").size(), is(0));
	}
	
	@Test
	public void testClientScriptIsAddedWhenAbsentAndSharedScriptIsPresent() {
		
		// given
		given(associatedScriptBundle.sharedScriptResource()).willReturn(scriptResource);
		given(scriptResource.type()).willReturn(ScriptResourceType.Shared);
		
		//when
		filter.filter(documentRequest);
		
		// then
		assertThat(document.select("script[type=text/javascript]").size(), is(3));
		assertThat(document.select("script[src=" + JQUERY_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + JJ_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + JJ_URI + "]").attr("data-jj-socket-url"), is(webSocketUri));
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
		assertThat(document.select("script[src=" + JQUERY_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + JJ_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + JJ_URI + "]").attr("data-jj-socket-url"), is(webSocketUri));
		assertThat(document.select("script[src=/" + scriptUri + ".js]").size(), is(0));
		assertThat(document.select("script[src=/" + scriptUri + ".shared.js]").size(), is(0));
	}
	
	@Test
	public void testMessagesAreAddedCorrectlyToTheScript() {
		
		// given
		List<JQueryMessage> messages = Arrays.asList(
			JQueryMessage.makeBind("a.fancybox", "", "click"),
			JQueryMessage.makeBind("a.fancyvideo", "", "click"),
			JQueryMessage.makeBind("#chatbox", "", "enter")
		);
		
		given(httpRequest.startupJQueryMessages()).willReturn(messages);
		filter = new ScriptHelperDocumentFilter(configuration, context, resourceFinder);
		
		// when 
		filter.filter(documentRequest);
		
		// then
		assertThat(document.select("#jj-connector-script").attr("data-jj-startup-messages"), is(messages.toString()));
	}

}
