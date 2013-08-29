package jj.http.server.servable.document;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static jj.resource.AssetResource.*;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import jj.configuration.Configuration;
import jj.http.HttpRequest;
import jj.http.server.servable.document.DocumentRequestProcessor;
import jj.http.server.servable.document.ScriptHelperDocumentFilter;
import jj.jjmessage.JJMessage;
import jj.resource.AssetResource;
import jj.resource.ResourceFinder;
import jj.resource.ScriptResource;
import jj.resource.ScriptResourceType;
import jj.script.CurrentScriptContext;
import jj.script.AssociatedScriptBundle;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScriptHelperDocumentFilterTest {
	
	static final String JQUERY_URI = "/" + JQUERY_JS;
	static final String JJ_SHA = "be03b9352e1e254cae9a58cff2b20e0c8d513e47";
	static final String JJ_URI = "/" + JJ_SHA + "/" + JJ_JS;
	
	String scriptUri;
	String socketUri;
	String webSocketUri;
	@Mock Configuration configuration;
	@Mock AssociatedScriptBundle associatedScriptBundle;
	@Mock CurrentScriptContext context;
	@Mock ScriptResource scriptResource;
	@Mock HttpRequest httpRequest;
	@Mock ResourceFinder resourceFinder;
	@Mock AssetResource jqueryJs;
	@Mock AssetResource jjJs;
	Document document;
	@Mock DocumentRequestProcessor documentRequestProcessor;

	ScriptHelperDocumentFilter filter;
	
	@Before
	public void before() {
		
		scriptUri = "/" + JJ_SHA + "/index/blahblahblah";
		socketUri = scriptUri + ".socket";
		webSocketUri = "ws://localhost:8080" + socketUri;
		
		when(associatedScriptBundle.toUri()).thenReturn(scriptUri);
		when(associatedScriptBundle.toSocketUri()).thenReturn(socketUri);
		
		when(context.associatedScriptBundle()).thenReturn(associatedScriptBundle);
		when(context.httpRequest()).thenReturn(httpRequest);
		when(context.documentRequestProcessor()).thenReturn(documentRequestProcessor);
		
		when(documentRequestProcessor.startupJJMessages()).thenReturn(Collections.<JJMessage>emptyList());
		when(httpRequest.host()).thenReturn("localhost:8080");

		document = Jsoup.parse("<html><head><title>what</title></head><body></body></html>");
		when(documentRequestProcessor.document()).thenReturn(document);
		when(documentRequestProcessor.httpRequest()).thenReturn(httpRequest);
		
		when(resourceFinder.findResource(AssetResource.class, JQUERY_JS))
			.thenReturn(jqueryJs);
		when(resourceFinder.findResource(AssetResource.class, JJ_JS))
			.thenReturn(jjJs);
		
		when(jqueryJs.uri()).thenReturn(JQUERY_URI);
		when(jqueryJs.baseName()).thenReturn(JQUERY_JS);
		
		when(jjJs.sha1()).thenReturn(JJ_SHA);
		when(jjJs.uri()).thenReturn(JJ_URI);
		
		given(configuration.get(DocumentConfiguration.class)).willReturn(new MockDocumentConfiguration());
		
		filter = new ScriptHelperDocumentFilter(configuration, context, resourceFinder);
	}
	
	@Test
	public void testClientAndSharedScriptsAddedWhenPresent() {
		
		// given
		given(associatedScriptBundle.clientScriptResource()).willReturn(scriptResource);
		given(associatedScriptBundle.sharedScriptResource()).willReturn(scriptResource);
		
		// when
		filter.filter(documentRequestProcessor);
		
		// then
		assertThat(document.select("script[type=text/javascript]").size(), is(4));
		assertThat(document.select("script[src=" + JQUERY_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + JJ_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + JJ_URI + "]").attr("data-jj-socket-url"), is(webSocketUri));
		assertThat(document.select("script[src=" + ScriptResourceType.Client.suffix(scriptUri)+ "]").size(), is(1));
		assertThat(document.select("script[src=" + ScriptResourceType.Shared.suffix(scriptUri) + "]").size(), is(1));
	}
	
	@Test
	public void testClientScriptIsAddedWhenPresentAndSharedScriptIsAbsent() {
		
		// given
		given(associatedScriptBundle.clientScriptResource()).willReturn(scriptResource);
		
		// when
		filter.filter(documentRequestProcessor);
		
		// then
		assertThat(document.select("script[type=text/javascript]").size(), is(3));
		assertThat(document.select("script[src=" + JQUERY_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + JJ_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + JJ_URI + "]").attr("data-jj-socket-url"), is(webSocketUri));
		assertThat(document.select("script[src=" + ScriptResourceType.Client.suffix(scriptUri)+ "]").size(), is(1));
		assertThat(document.select("script[src=" + ScriptResourceType.Shared.suffix(scriptUri) + "]").size(), is(0));
	}
	
	@Test
	public void testClientScriptIsAddedWhenAbsentAndSharedScriptIsPresent() {
		
		// given
		given(associatedScriptBundle.sharedScriptResource()).willReturn(scriptResource);
		
		//when
		filter.filter(documentRequestProcessor);
		
		// then
		assertThat(document.select("script[type=text/javascript]").size(), is(3));
		assertThat(document.select("script[src=" + JQUERY_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + JJ_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + JJ_URI + "]").attr("data-jj-socket-url"), is(webSocketUri));
		assertThat(document.select("script[src=" + ScriptResourceType.Client.suffix(scriptUri)+ "]").size(), is(0));
		assertThat(document.select("script[src=" + ScriptResourceType.Shared.suffix(scriptUri) + "]").size(), is(1));
	}
	
	@Test
	public void testDefaultScriptsAreAddedWhenNothingIsPresent() {
		
		// given our default state
		
		// when
		filter.filter(documentRequestProcessor);
		
		//then
		assertThat(document.select("script[type=text/javascript]").size(), is(2));
		assertThat(document.select("script[src=" + JQUERY_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + JJ_URI + "]").size(), is(1));
		assertThat(document.select("script[src=" + JJ_URI + "]").attr("data-jj-socket-url"), is(webSocketUri));
		assertThat(document.select("script[src=" + ScriptResourceType.Client.suffix(scriptUri)+ "]").size(), is(0));
		assertThat(document.select("script[src=" + ScriptResourceType.Shared.suffix(scriptUri) + "]").size(), is(0));
	}
	
	@Test
	public void testMessagesAreAddedCorrectlyToTheScript() {
		
		// given
		List<JJMessage> messages = Arrays.asList(
			JJMessage.makeBind("a.fancybox", "", "click"),
			JJMessage.makeBind("a.fancyvideo", "", "click"),
			JJMessage.makeBind("#chatbox", "", "enter")
		);
		
		given(documentRequestProcessor.startupJJMessages()).willReturn(messages);
		filter = new ScriptHelperDocumentFilter(configuration, context, resourceFinder);
		
		// when 
		filter.filter(documentRequestProcessor);
		
		// then
		assertThat(document.select("#jj-connector-script").attr("data-jj-startup-messages"), is(messages.toString()));
	}

}
