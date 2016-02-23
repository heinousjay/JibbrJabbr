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

import java.util.HashMap;
import java.util.Map;

import jj.http.server.resource.StaticResource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceCreatorsTest {
	
	@Mock SimpleResourceCreator<? extends AbstractResource<?>, ?> creator;
	
	Map<Class<? extends AbstractResource<?>>, SimpleResourceCreator<? extends AbstractResource<?>, ?>>
		resourceCreators = new HashMap<>();

	@Test
	public void testKnownResourceTypeNames() {
		resourceCreators.put(StaticResource.class, creator);
		resourceCreators.put(Sha1Resource.class, creator);
		
		ResourceCreators rc = new ResourceCreators(resourceCreators);
		
		// contains here, because order matters
		assertThat(rc.knownResourceTypeNames(), contains(
			"jj.http.server.resource.StaticResource",
			"jj.resource.Sha1Resource"
		));
	}

}
