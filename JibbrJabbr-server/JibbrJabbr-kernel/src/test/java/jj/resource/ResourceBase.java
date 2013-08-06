/*
 *    Copyright 2012 Jason Miller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jj.resource;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.file.Files;
import java.nio.file.Path;
import jj.SHA1Helper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * base test for resource testing
 * @author jason
 *
 */
public abstract class ResourceBase<U extends Resource, T extends ResourceCreator<U>> extends RealResourceBase {
	
	ResourceInstanceModuleCreator instanceModuleCreator;
	@Mock Injector injector;
	
	U resource;
	
	T toTest;
	
	protected Object[] args() {
		return AbstractFileResource.EMPTY_ARGS;
	}
	
	protected ResourceCacheKey cacheKey() {
		return toTest.cacheKey(baseName(), args());
	}
	
	protected abstract String baseName();
	
	protected abstract Path path();
	
	protected abstract U resource() throws Exception;
	
	protected abstract T toTest();
	
	protected void before() throws Exception {}

	@SuppressWarnings("unchecked")
	protected void configureInjector(U resource) {
		given(injector.getInstance(BDDMockito.any(Class.class))).willReturn(resource);
	}
	
	@Before
	public final void setup() throws Exception {
		
		before();
		
		instanceModuleCreator = new ResourceInstanceModuleCreator(injector);
		
		toTest = toTest();
		
		resource = resource();
		
		given(injector.createChildInjector(BDDMockito.any(Module.class))).willReturn(injector);
		configureInjector(resource);
	}
	
	@Test
	public void test() throws Exception {
		
		U created = toTest.create(baseName(), args()); 
		
		testFileResource(created);
		
		assertThat(created, is(resource));
		
		resourceAssertions();
	}
	
	protected void resourceAssertions() throws Exception {
		
	}
		

	protected void testFileResource(final Resource resource1) throws Exception {
		
		// well this is weirdly ugly haha
		final Path path = resource1.path();
		
		assertTrue(resource1.baseName() + " does not exist", Files.exists(path));
		
		assertThat(resource1, is(instanceOf(AbstractResource.class)));
		 
		final byte[] bytes = Files.readAllBytes(path);
		assertThat(resource1, is(notNullValue()));
		assertThat(resource1.baseName(), is(notNullValue()));
		assertThat(resource1.baseName(), not(Matchers.startsWith("/")));
		assertThat(resource1.lastModified(), is(Files.getLastModifiedTime(path)));
		assertThat(((AbstractResource)resource1).needsReplacing(), is(false));
		
		if (!(resource1 instanceof CssResource)) { 
			assertThat(resource1.sha1(), is(SHA1Helper.keyFor(bytes))); 
		}
		
		if (resource1 instanceof LoadedResource) {
			ByteBuf buf = ((LoadedResource)resource1).bytes();
			buf.readBytes(Unpooled.buffer(buf.readableBytes()));
			// this test is to ensure that all buffers are wrapped before returning
			// since this has caught me out twice now
			assertThat(((LoadedResource)resource1).bytes().readableBytes(), greaterThan(0));
		}
	}
}
