package jj.http.server.servable;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import jj.JJ;
import jj.configuration.Configuration;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.http.server.servable.CssServable;
import jj.http.server.servable.RequestProcessor;
import jj.resource.CssResource;
import jj.resource.ResourceFinder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CssServableTest {
	
	Path basePath = Paths.get(JJ.uri(CssServableTest.class)).getParent();
	
	@Mock Configuration configuration;
	@Mock ResourceFinder resourceFinder;
	@Mock CssResource cssResource;
	
	@Mock HttpRequest request;
	@Mock HttpResponse response;
	
	@Before
	public void before() {
		when(configuration.basePath()).thenReturn(basePath);
	}

	@Test
	public void test() throws IOException {
		
		// given
		given(request.uri()).willReturn("/style.css");
		given(resourceFinder.loadResource(CssResource.class, "style.css", true)).willReturn(cssResource);
		given(cssResource.path()).willReturn(basePath.resolve("style.css"));
		
		// when
		CssServable cssServable = new CssServable(configuration, resourceFinder);
		
		// then
		assertThat(cssServable.isMatchingRequest(request), is(true));
		
		// when
		RequestProcessor requestProcessor = cssServable.makeRequestProcessor(request, response);
		
		// then
		assertThat(requestProcessor, is(notNullValue()));
	}
}
