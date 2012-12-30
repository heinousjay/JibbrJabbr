package jj.servable;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;

import java.nio.file.Path;
import java.nio.file.Paths;

import jj.servable.CssServable;
import jj.webbit.JJHttpRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CssServableResourceTest {
	
	Path basePath = Paths.get("/Users/jason/");
	
	@Mock JJHttpRequest request;
	
	@Before
	public void before() {
	}

	@Test
	public void test() {
		
		// given
		given(request.uri())
			.willReturn("/style/da39a3ee5e6b4b0d3255bfef95601890afd80709.css")
			.willReturn("/not/a/path/to/servable.css");
		
		// when
		CssServable cssServableResource = new CssServable(basePath);
		
		// then
		assertThat(cssServableResource.isMatchingRequest(request), is(true));
		assertThat(cssServableResource.isMatchingRequest(request), is(false));
	}
}