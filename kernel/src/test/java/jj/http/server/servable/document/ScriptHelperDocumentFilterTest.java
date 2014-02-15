package jj.http.server.servable.document;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static jj.configuration.Assets.*;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import jj.configuration.AppLocation;
import jj.configuration.Configuration;
import jj.http.HttpRequest;
import jj.http.server.servable.document.DocumentRequestProcessor;
import jj.http.server.servable.document.ScriptHelperDocumentFilter;
import jj.jjmessage.JJMessage;
import jj.resource.ResourceFinder;
import jj.resource.document.DocumentScriptEnvironment;
import jj.resource.script.ScriptResource;
import jj.resource.script.ScriptResourceType;
import jj.resource.stat.ic.StaticResource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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
	@Mock DocumentScriptEnvironment documentScriptEnvironment;
	@Mock ScriptResource scriptResource;
	@Mock HttpRequest httpRequest;
	@Mock StaticResource jqueryJs;
	@Mock StaticResource jjJs;
	Document document;
	@Mock DocumentRequestProcessor documentRequestProcessor;


	@Mock Configuration configuration;
	@Mock ResourceFinder resourceFinder;
	
	@InjectMocks ScriptHelperDocumentFilter filter;
	
	@Before
	public void before() {
		
		scriptUri = "/" + JJ_SHA + "/index/blahblahblah";
		socketUri = scriptUri + ".socket";
		webSocketUri = "ws://localhost:8080" + socketUri;
		
		given(documentScriptEnvironment.uri()).willReturn(scriptUri);
		given(documentScriptEnvironment.socketUri()).willReturn(socketUri);
		given(documentScriptEnvironment.hasServerScript()).willReturn(true);
		given(documentRequestProcessor.documentScriptEnvironment()).willReturn(documentScriptEnvironment);
		
		given(documentRequestProcessor.startupJJMessages()).willReturn(Collections.<JJMessage>emptyList());
		given(httpRequest.host()).willReturn("localhost:8080");

		document = Jsoup.parse("<html><head><title>what</title></head><body></body></html>");
		given(documentRequestProcessor.document()).willReturn(document);
		given(documentRequestProcessor.httpRequest()).willReturn(httpRequest);
		
		given(resourceFinder.findResource(StaticResource.class, AppLocation.Assets, JQUERY_JS))
			.willReturn(jqueryJs);
		given(resourceFinder.findResource(StaticResource.class, AppLocation.Assets, JJ_JS))
			.willReturn(jjJs);
		
		given(jqueryJs.uri()).willReturn(JQUERY_URI);
		given(jqueryJs.name()).willReturn(JQUERY_JS);
		
		given(jjJs.sha1()).willReturn(JJ_SHA);
		given(jjJs.uri()).willReturn(JJ_URI);
		
		given(configuration.get(DocumentConfiguration.class)).willReturn(new MockDocumentConfiguration());
	}
	
	@Test
	public void testNothingIsAddedWhenThereIsNoServerScript() {
		
		// given
		given(documentScriptEnvironment.hasServerScript()).willReturn(false);
		
		// when
		filter.filter(documentRequestProcessor);
		
		assertThat(document.select("script[type=text/javascript]").size(), is(0));
	}
	
	@Test
	public void testClientAndSharedScriptsAddedWhenPresent() {
		
		// given
		given(documentScriptEnvironment.clientScriptResource()).willReturn(scriptResource);
		given(documentScriptEnvironment.sharedScriptResource()).willReturn(scriptResource);
		
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
		given(documentScriptEnvironment.clientScriptResource()).willReturn(scriptResource);
		
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
		given(documentScriptEnvironment.sharedScriptResource()).willReturn(scriptResource);
		
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
		filter = new ScriptHelperDocumentFilter(configuration, resourceFinder);
		
		// when 
		filter.filter(documentRequestProcessor);
		
		// then
		assertThat(document.select("#jj-connector-script").attr("data-jj-startup-messages"), is(messages.toString()));
	}

}
