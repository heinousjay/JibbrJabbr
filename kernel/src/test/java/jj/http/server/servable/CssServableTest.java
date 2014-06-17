package jj.http.server.servable;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;

import jj.configuration.resolution.AppLocation;
import jj.css.CssResource;
import jj.http.server.servable.CssServable;
import jj.http.server.servable.RequestProcessor;
import jj.http.server.uri.URIMatch;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;


public class CssServableTest extends ServableTestBase {
	
	@Mock CssResource cssResource;
	
	CssServable cs;
	
	@Before
	public void before() {
		
		cs = new CssServable(app, resourceFinder);
	}

	@Test
	public void testBasicOperation() throws IOException {
		
		// given
		given(request.uriMatch()).willReturn(new URIMatch("/style.css"));
		given(resourceFinder.loadResource(CssResource.class, AppLocation.Base, "style.css", true)).willReturn(cssResource);
		given(cssResource.path()).willReturn(appPath.resolve("style.css"));
		
		// then
		assertThat(cs.isMatchingRequest(request.uriMatch()), is(true));
		
		// when
		RequestProcessor requestProcessor = cs.makeRequestProcessor(request, response);
		
		// then
		assertThat(requestProcessor, is(notNullValue()));
	}
	
	@Test
	public void testOutsideApplicationIsRejected() throws Exception {
		
		given(request.uriMatch()).willReturn(new URIMatch("/../not-servable/style.css"));
		
		RequestProcessor rp = cs.makeRequestProcessor(request, response);
		
		assertThat(rp, is(nullValue()));
	}
}
