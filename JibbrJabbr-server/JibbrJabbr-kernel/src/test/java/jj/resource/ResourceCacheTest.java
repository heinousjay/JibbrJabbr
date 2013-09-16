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
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

import jj.resource.html.HtmlResource;
import jj.resource.stat.ic.StaticResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */


@RunWith(MockitoJUnitRunner.class)
public class ResourceCacheTest extends RealResourceBase {
	
	URI uri = URI.create("/resource1");
	
	@Mock StaticResource sr;
	
	@Mock ResourceCreator<StaticResource> src;
	
	@Mock HtmlResource hr;
	
	@Mock ResourceCreator<HtmlResource> hrc;
	
	ResourceCacheKey sKey;
	
	ResourceCacheKey hKey;
	
	ResourceCacheImpl rc;
	
	@Before
	public void before() {
		
		HashMap<Class<? extends Resource>, ResourceCreator<? extends Resource>> map = new HashMap<>();
		map.put(StaticResource.class, src);
		map.put(HtmlResource.class, hrc);
		rc = new ResourceCacheImpl(new ResourceCreators(map));
		
		sKey = new ResourceCacheKey(StaticResource.class, uri);
		hKey = new ResourceCacheKey(HtmlResource.class, uri);
		
		given(src.cacheKey(uri)).willReturn(sKey);
		given(hrc.cacheKey(uri)).willReturn(hKey);
	}

	@Test
	public void testOperationsByUri() throws IOException {
		
		// given
		rc.putIfAbsent(sKey, sr);
		rc.putIfAbsent(hKey, hr);
		// when
		List<Resource> resources = rc.findAllByUri(uri);
		
		// then
		assertThat(resources.size(), is(2));
		assertThat(resources, containsInAnyOrder((Resource)sr, (Resource)hr));
	}

}
