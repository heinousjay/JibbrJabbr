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

import static org.mockito.BDDMockito.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import jj.CoreConfiguration;
import jj.JJ;
import jj.configuration.Configuration;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
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
	
	Path appPath = Paths.get(JJ.uri(ServableTestBase.class)).getParent();
	
	@Mock CoreConfiguration coreConfiguration;
	@Mock Configuration configuration;
	@Mock ResourceFinder resourceFinder;
	
	@Mock HttpRequest request;
	@Mock HttpResponse response;
	
	@Before
	public void baseBefore() {
		given(configuration.get(CoreConfiguration.class)).willReturn(coreConfiguration);
		given(coreConfiguration.appPath()).willReturn(appPath);
	}

}
