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
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.JJServerStartupListener;
import jj.execution.IOTask;
import jj.execution.JJExecutors;
import jj.resource.ResourceFinder;
import jj.resource.config.ConfigResource;

/**
 * ensures that the configuration file for the application
 * is available to the system.
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
		
		try {
			executors.execute(new IOTask("preloading configuration script") {
				
				@Override
				public void run() {
					resourceFinder.loadResource(ConfigResource.class, ConfigResource.CONFIG_JS);
					latch.countDown();
				}
			}).get(1, SECONDS);
		} catch (TimeoutException te) {
		// 1 second is kinda arbitrary but really
		// if it takes any kind of time at all,
		// we're hosed
			throw new AssertionError("timed out loading the configuration", te);
		}
	}

	@Override
	public Priority startPriority() {
		// needs to run before anything else, since
		// any other component may need configuring
		// and we don't want to flip-flop into IO
		// threads
		return Priority.Highest;
	}
}
