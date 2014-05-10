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
package jj.configuration.resolution;

import static org.mockito.Mockito.mock;

import java.nio.file.Path;

import jj.Base;
import jj.configuration.Arguments;
import jj.configuration.Location;
import jj.configuration.resolution.AppLocation;
import jj.configuration.resolution.Application;

/**
 * @author jason
 *
 */
public class MockApplication extends Application {

	private final Path basePath;
	
	/**
	 * @param arguments
	 */
	public MockApplication() {
		super(mock(Arguments.class), new MockAssets(), new MockAPIModules());
		basePath = Base.path;
	}
	
	public MockApplication(final Path basePath) {
		super(mock(Arguments.class), new MockAssets(), new MockAPIModules());
		this.basePath = basePath;
	}

	@Override
	public Path resolvePath(Location base, String name) {
		if (base != AppLocation.Assets && base != AppLocation.Virtual) {
			return basePath.resolve(((AppLocation)base).path()).resolve(name);
		}
		return super.resolvePath(base, name);
	}
}
