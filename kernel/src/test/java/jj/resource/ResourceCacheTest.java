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

import static jj.server.ServerLocation.Virtual;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import jj.event.MockPublisher;

import jj.server.ServerLocation;
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
public class ResourceCacheTest {


	
	MyResource resource1;
	MyResource resource2;
	TestDateResource testDateResource1;
	
	@Mock SimpleResourceCreator<Void, MyResource> creator1;
	@Mock SimpleResourceCreator<Date, TestDateResource> creator2;

	ResourceCache rc;

	MockPublisher publisher;
	
	@Before
	public void before() {

		publisher = new MockPublisher();
		
		HashMap<Class<? extends AbstractResource<?>>, SimpleResourceCreator<?, ? extends AbstractResource<?>>> map = new HashMap<>();
		map.put(MyResource.class, creator1);
		map.put(TestDateResource.class, creator2);
		rc = new ResourceCache(new ResourceCreators(map));

		resource1 = new MyResource("resource1", publisher);
		resource2 = new MyResource("resource2", publisher);
		ResourceIdentifier<TestDateResource, Date> identifier = ResourceIdentifierHelper.make(
			TestDateResource.class,
			Virtual,
			"resource1",
			new Date()
		);
		testDateResource1 = new TestDateResource(identifier);

	}
	
	@Test
	public void testShutdownBehavior() throws Exception {
		
		rc.putIfAbsent(resource1);
		assertThat(rc.get(resource1.identifier()), is(resource1));
		rc.on(null);
		assertThat(rc.get(resource1.identifier()), is(nullValue()));
		
	}

	@Test
	public void testOperationsByPath() throws IOException {
		
		// given
		rc.putIfAbsent(resource1);
		rc.putIfAbsent(testDateResource1);
		// when
		List<Resource<?>> resources = rc.findAllByPath(resource1.path());
		
		// then
		assertThat(resources, hasSize(2));
		assertThat(resources, containsInAnyOrder(resource1, testDateResource1));
	}

}
