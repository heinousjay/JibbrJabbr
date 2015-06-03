package jj.resource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static jj.server.ServerLocation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import jj.server.ServerLocation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PathResolverImplTest {
	
	PathResolverImpl pri;
	
	@Mock LocationResolver locationResolver;
	
	@Before
	public void before() {
		pri = new PathResolverImpl(Collections.singletonMap(ServerLocation.class, locationResolver));
	}

	@Test
	public void testResolvePath() {
		
		String name = "jay";
		Path jay = Paths.get(name);
		
		given(locationResolver.resolvePath(Virtual, name)).willReturn(jay);
		
		Path result = pri.resolvePath(Virtual, name);
		
		assertThat(result, is(jay.normalize().toAbsolutePath()));
	}

}
