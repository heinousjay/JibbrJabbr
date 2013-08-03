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

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import jj.execution.MockJJExecutors;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceFinderImplTest {
	
	ResourceCacheKey cacheKey = new ResourceCacheKey(URI.create("/"));
	
	ResourceCache resourceCache;
	Set<ResourceCreator<?>> resourceCreators;
	@Mock ResourceCreator<? extends Resource> resourceCreator;
	@Mock AbstractResource resource;
	@Mock ResourceWatchService resourceWatchService;
	MockJJExecutors executors;
	ResourceFinderImpl rfi;
	
	@Before
	public void before() {
		resourceCache = new ResourceCache();
		resourceCreators = new HashSet<>();
		resourceCreators.add(resourceCreator);
		executors = new MockJJExecutors();
		
		rfi = new ResourceFinderImpl(resourceCache, resourceCreators, resourceWatchService, executors);
	}

	@Ignore
	@Test
	public void testLoadResource() {
		
		given(resourceCreator.cacheKey(anyString(), anyVararg())).willReturn(cacheKey);
		
		resourceCache.put(cacheKey, resource);
		
		Resource r = rfi.findResource(resource);
		
		assertThat(resource, is(sameInstance(r)));
	}
}
