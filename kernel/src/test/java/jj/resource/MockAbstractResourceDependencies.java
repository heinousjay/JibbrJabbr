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

import static org.mockito.BDDMockito.*;
import static jj.configuration.resolution.AppLocation.Base;
import jj.configuration.Location;
import jj.event.Publisher;
import jj.resource.AbstractResource.Dependencies;

/**
 * to simplify resource testing
 * 
 * @author jason
 *
 */
public class MockAbstractResourceDependencies extends Dependencies {
	
	public final DirectoryResource rootDirectory = mock(DirectoryResource.class);

	public MockAbstractResourceDependencies(Location base) {
		super(mock(ResourceKey.class), base, mock(Publisher.class), mock(ResourceFinder.class));
	}
	
	public MockAbstractResourceDependencies(ResourceKey resourceKey, Location base) {
		super(resourceKey, base, mock(Publisher.class), mock(ResourceFinder.class));
	}
	
	public MockAbstractResourceDependencies(ResourceKey resourceKey, Location base, Publisher publisher) {
		super(resourceKey, base, publisher, mock(ResourceFinder.class));
	}
	
	{
		given(resourceFinder.findResource(DirectoryResource.class, Base, "")).willReturn(rootDirectory);
	}

	public ResourceFinder resourceFinder() {
		return resourceFinder;
	}
	
	public Publisher publisher() {
		return publisher;
	}
	
	public ResourceKey resourceKey() {
		return resourceKey;
	}
	
	public Location base() {
		return base;
	}
}
