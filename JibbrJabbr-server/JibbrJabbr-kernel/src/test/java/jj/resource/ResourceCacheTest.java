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
import java.nio.file.Path;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class ResourceCacheTest extends RealResourceBase {
	
	ResourceCacheImpl rc;
	
	@Before
	public void before() {
		rc = new ResourceCacheImpl(MockResourceCreators.realized(configuration));
	}

	@Test
	public void testOperationsByUri() throws IOException {
		
		// given
		String name = "index.html";
		Path path = appPath.resolve(name);
		StaticResource sr = new StaticResource(MockResourceCreators.src.cacheKey(name), path, name);
		name = "index";
		HtmlResource hr = new HtmlResource(configuration, MockResourceCreators.hrc.cacheKey(name), name, path);
		
		rc.put(sr.cacheKey(), sr);
		rc.put(hr.cacheKey(), hr);
		
		URI uri = path.toUri();
		
		// when
		List<Resource> resources = rc.findAllByUri(uri);
		
		// then
		assertThat(resources.size(), is(2));
		assertThat(resources, containsInAnyOrder((Resource)sr, (Resource)hr));
	}

}
