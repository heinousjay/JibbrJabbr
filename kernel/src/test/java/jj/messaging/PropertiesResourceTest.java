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
package jj.messaging;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import jj.Base;
import jj.configuration.resolution.AppLocation;
import jj.resource.AbstractResource.Dependencies;
import jj.resource.ResourceKey;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PropertiesResourceTest {
	
	@Mock ResourceKey resourceKey;

	@Test
	public void test() throws Exception {
		Dependencies dependencies = new Dependencies(resourceKey, AppLocation.Base);
		
		PropertiesResource resource = new PropertiesResource(dependencies, Base.appPath().resolve("index.properties"), "test");
		
		assertThat(resource.properties().size(), is(4));
		assertThat(resource.properties().keySet(), containsInAnyOrder("topic", "title", "button", "welcome"));
		assertThat(resource.properties().get("topic"), is("Topic:"));
		assertThat(resource.properties().get("title"), is("JAYCHAT!"));
		assertThat(resource.properties().get("button"), is("Say"));
		assertThat(resource.properties().get("welcome"), is("Welcome to JayChat!"));
	}

}
