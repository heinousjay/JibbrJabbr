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

import java.util.concurrent.Executors;

import jj.execution.JJExecutors;
import jj.resource.ResourceFinder;
import jj.resource.config.ConfigResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationScriptPreloaderTest {

	@Mock JJExecutors executors;
	@Mock ResourceFinder resourceFinder;
	
	@Before
	public void before() {
		given(executors.ioExecutor()).willReturn(Executors.newCachedThreadPool());
	}
	
	@After
	public void after() {
		executors.ioExecutor().shutdownNow();
	}
	
	@Test
	public void test() throws Exception {
		
		ConfigurationScriptPreloader csp = new ConfigurationScriptPreloader(executors, resourceFinder);
		csp.start();
		
		verify(resourceFinder).loadResource(ConfigResource.class, ConfigResource.CONFIG_JS);
	}

}
