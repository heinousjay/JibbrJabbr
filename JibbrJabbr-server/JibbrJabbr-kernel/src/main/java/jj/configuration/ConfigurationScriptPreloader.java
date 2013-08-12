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

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.JJServerStartupListener;
import jj.execution.JJExecutors;
import jj.execution.JJRunnable;
import jj.resource.ConfigResource;
import jj.resource.ResourceFinder;

/**
 * ensures that the configuration file for the application
 * is available to the system
 * 
 * @author jason
 *
 */
@Singleton
class ConfigurationScriptPreloader implements JJServerStartupListener {
	
	private final JJExecutors executors;
	private final ResourceFinder resourceFinder;
	
	@Inject
	ConfigurationScriptPreloader(
		final JJExecutors executors,
		final ResourceFinder resourceFinder
	) {
		this.executors = executors;
		this.resourceFinder = resourceFinder;
	}

	@Override
	public void start() throws Exception {
		final CountDownLatch latch = new CountDownLatch(1);
		executors.ioExecutor().submit(new JJRunnable("preloading configuration script") {
			
			@Override
			public void doRun() {
				resourceFinder.loadResource(ConfigResource.class, ConfigResource.CONFIG_JS);
				latch.countDown();
			}
		});
		
		if (!latch.await(3, SECONDS)) {
			throw new AssertionError("timed out loading the configuration");
		}
	}

	@Override
	public Priority startPriority() {
		return Priority.Highest;
	}
}
