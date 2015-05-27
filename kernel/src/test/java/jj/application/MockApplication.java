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
package jj.application;

import static jj.system.ServerLocation.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.nio.file.Path;
import java.nio.file.Paths;

import jj.Base;
import jj.application.AppLocation;
import jj.application.Application;
import jj.configuration.Arguments;
import jj.resource.Location;
import jj.system.Server;

/**
 * @author jason
 *
 */
public class MockApplication extends Application {
	
	private static Server mockServer() {
		Server result = mock(Server.class);
		given(result.path()).willReturn(Paths.get(""));
		return result;
	}

	private final Path basePath;
	
	/**
	 * @param arguments
	 */
	public MockApplication() {
		super(mock(Arguments.class), mockServer());
		basePath = Base.path;
	}
	
	public MockApplication(final Path basePath) {
		super(mock(Arguments.class), mockServer());
		this.basePath = basePath;
	}
	
	@Override
	public Path path() {
		return basePath;
	}
	
	@Override
	public boolean pathInBase(Path path) {
		return path.startsWith(basePath);
	}

	@Override
	public Path resolvePath(Location base, String name) {
		if (base != Assets && base != Virtual) {
			return basePath.resolve(((AppLocation)base).path()).resolve(name);
		}
		return super.resolvePath(base, name);
	}
}
