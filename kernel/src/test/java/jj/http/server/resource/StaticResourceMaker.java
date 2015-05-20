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
package jj.http.server.resource;

import static org.mockito.BDDMockito.*;
import jj.application.AppLocation;
import jj.event.Publisher;
import jj.http.server.resource.StaticResource;
import jj.resource.MockAbstractResourceDependencies;
import jj.resource.PathResolver;
import jj.resource.ResourceKey;

/**
 * @author jason
 *
 */
public class StaticResourceMaker {
	public static StaticResource make(PathResolver app, AppLocation base, String name) throws Exception {
		
		ResourceKey resourceKey = mock(ResourceKey.class);
		Publisher publisher = mock(Publisher.class);
		
		return new StaticResource(new MockAbstractResourceDependencies(resourceKey, base, name, publisher), app.resolvePath(base, name), app);
	}
}
