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
package jj.http.server.servable;

import java.nio.file.Path;
import java.nio.file.Paths;

import jj.JJ;
import jj.http.server.HttpServerRequest;
import jj.http.server.HttpServerResponse;
import jj.resource.ResourceFinder;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class ServableTestBase {
	
	Path appPath;
	
	@Mock ResourceFinder resourceFinder;
	
	@Mock HttpServerRequest request;
	@Mock HttpServerResponse response;
	
	@Before
	public void baseBefore() throws Exception {
		
		String path = 
			JJ.resourcePath(ServableTestBase.class).replace(ServableTestBase.class.getSimpleName() + ".class", "0.txt");
		
		appPath = Paths.get(ServableTestBase.class.getResource(path).toURI()).getParent();
	}

}
