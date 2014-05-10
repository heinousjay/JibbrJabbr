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
import static org.mockito.BDDMockito.*;

import java.util.Locale;

import jj.Base;
import jj.configuration.resolution.AppLocation;
import jj.resource.AbstractResource.Dependencies;
import jj.resource.ResourceFinder;
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
public class MessagesResourceTest {

	@Mock ResourceKey resourceKey;
	
	@Mock ResourceFinder resourceFinder;
	
	Dependencies dependencies = new Dependencies(resourceKey, AppLocation.Virtual);
	
	String name = "index";
	
	@Test
	public void test() throws Exception {
		
		PropertiesResource index_base = new PropertiesResource(dependencies, Base.appPath().resolve("index.properties"), name);
		given(resourceFinder.loadResource(PropertiesResource.class, AppLocation.Base, "index.properties")).willReturn(index_base);
		
		PropertiesResource index_en = new PropertiesResource(dependencies, Base.appPath().resolve("index_en.properties"), name);
		given(resourceFinder.loadResource(PropertiesResource.class, AppLocation.Base, "index_en.properties")).willReturn(index_en);
		
		PropertiesResource index_us = new PropertiesResource(dependencies, Base.appPath().resolve("index_en_US.properties"), name);
		given(resourceFinder.loadResource(PropertiesResource.class, AppLocation.Base, "index_en_US.properties")).willReturn(index_us);
		
		MessagesResource resource = new MessagesResource(dependencies, name, Locale.US, resourceFinder);
		
		assertThat(resource.message("title"), is("JAYCHAT!"));
		assertThat(resource.message("topic"), is("US TOPIC"));
		assertThat(resource.message("welcome"), is("EN WELCOME"));
		
		System.out.println(resource.sha1());
	}

}
