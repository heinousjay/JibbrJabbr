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
package jj.i18n;

import static jj.server.ServerLocation.Virtual;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Locale;

import jj.Base;
import jj.application.AppLocation;
import jj.i18n.MessagesResource;
import jj.i18n.PropertiesResource;
import jj.resource.MockAbstractResourceDependencies;
import jj.resource.ResourceFinder;

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
	
	@Mock ResourceFinder resourceFinder;
	
	String name = "index";

	MockAbstractResourceDependencies propertiesDependencies =
		new MockAbstractResourceDependencies(PropertiesResource.class, AppLocation.Private, "index.properties");
	
	MockAbstractResourceDependencies messageDependencies =
		new MockAbstractResourceDependencies(MessagesResource.class, Virtual, name, Locale.US);
	
	@Test
	public void test() throws Exception {
		
		PropertiesResource index_base = new PropertiesResource(propertiesDependencies, Base.appPath().resolve("index.properties"));
		given(resourceFinder.loadResource(PropertiesResource.class, AppLocation.Private, "index.properties")).willReturn(index_base);
		
		PropertiesResource index_en = new PropertiesResource(propertiesDependencies, Base.appPath().resolve("index_en.properties"));
		given(resourceFinder.loadResource(PropertiesResource.class, AppLocation.Private, "index_en.properties")).willReturn(index_en);
		
		PropertiesResource index_us = new PropertiesResource(propertiesDependencies, Base.appPath().resolve("index_en_US.properties"));
		given(resourceFinder.loadResource(PropertiesResource.class, AppLocation.Private, "index_en_US.properties")).willReturn(index_us);
		
		MessagesResource resource = new MessagesResource(messageDependencies, Locale.US, resourceFinder);
		
		assertThat(resource.message("title"), is("JAYCHAT!"));
		assertThat(resource.message("topic"), is("US TOPIC"));
		assertThat(resource.message("welcome"), is("EN WELCOME"));
	}

}
