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

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class ResourceCacheTest extends ResourceBase {
	
	ResourceCache rc;
	StaticResourceCreator src;
	HtmlResourceCreator hrc;
	
	
	@Before
	public void before() {
		src = new StaticResourceCreator(configuration);
		hrc = new HtmlResourceCreator(configuration);
		HashSet<ResourceCreator<? extends Resource>> resourceCreators = new HashSet<>();
		resourceCreators.add(src);
		resourceCreators.add(hrc);
		rc = new ResourceCache(resourceCreators);
	}

	@Test
	public void testOperationsByUri() throws IOException {
		StaticResource sr = src.create("index.html");
		HtmlResource hr = hrc.create("index");
		
		rc.put(src.cacheKey("index.html"), sr);
		rc.put(hrc.cacheKey("index"), hr);
		
		URI uri = basePath.resolve("index.html").toUri();
		
		List<Resource> resources = rc.findAllByUri(uri);
		
		assertThat(resources.size(), is(2));
		assertThat(resources, containsInAnyOrder((Resource)sr, (Resource)hr));
		
		StaticResource sr2 = src.create("blank.gif");
		rc.put(src.cacheKey("blank.gif"), sr2);
		rc.removeAllByUri(uri);
		
		assertThat(rc.size(), is(1));
		assertThat(rc.values(), contains((Resource)sr2));
	}

}
