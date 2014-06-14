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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static jj.configuration.resolution.AppLocation.*;
import static jj.configuration.ConfigurationScriptEnvironmentCreator.CONFIG_SCRIPT_NAME;

import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.JJServerStartupListener;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.resource.ResourceLoader;

/**
 * ensures that the configuration file for the application
 * is available to the system on startup
 * 
 * @author jason
 *
 */
@Singleton
@Subscriber
class ConfigurationScriptLoader implements JJServerStartupListener {
	
	private final ResourceLoader resourceLoader;
	private final CountDownLatch latch = new CountDownLatch(1);
	
	@Inject
	ConfigurationScriptLoader(final ResourceLoader resourceFinder) {
		this.resourceLoader = resourceFinder;
	}
	
	@Listener
	void configurationLoaded(ConfigurationLoaded configurationLoaded) {
		latch.countDown();
	}
	
	private void load() {
		resourceLoader.loadResource(ConfigurationScriptEnvironment.class, Virtual, CONFIG_SCRIPT_NAME);
	}

	@Override
	public void start() throws Exception {
		
		load();
		
		boolean success = latch.await(500, MILLISECONDS);
		assert success : "configuration didn't load in 500 milliseconds";
	}

	@Override
	public Priority startPriority() {
		return Priority.Middle;
	}
}