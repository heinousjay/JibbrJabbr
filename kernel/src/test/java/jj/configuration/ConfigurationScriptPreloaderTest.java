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
package jj.configuration;

import static org.mockito.BDDMockito.*;
import static jj.configuration.resolution.AppLocation.*;
import static jj.configuration.ConfigurationScriptEnvironmentCreator.CONFIG_SCRIPT_NAME;

import jj.resource.ResourceLoader;
import jj.resource.config.ConfigResource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationScriptPreloaderTest {

	@Mock ResourceLoader resourceLoader;
	
	@InjectMocks ConfigurationScriptPreloader csp;
	
	@Test
	public void test() throws Exception {
		csp.configurationLoaded(null);
		csp.start();
		
		verify(resourceLoader).loadResource(ConfigResource.class, Base, ConfigResource.CONFIG_JS);
		verify(resourceLoader).loadResource(ConfigurationScriptEnvironment.class, Virtual, CONFIG_SCRIPT_NAME);
	}

}
