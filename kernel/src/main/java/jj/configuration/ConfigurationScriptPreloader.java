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

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.JJServerStartupListener;
import jj.configuration.resolution.AppLocation;
import jj.event.Publisher;
import jj.execution.TaskRunner;
import jj.resource.ResourceTask;
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
	
	private final TaskRunner taskRunner;
	private final ResourceFinder resourceFinder;
	private final Publisher publisher;
	
	@Inject
	ConfigurationScriptPreloader(
		final TaskRunner taskRunner,
		final ResourceFinder resourceFinder,
		final Publisher publisher
	) {
		this.taskRunner = taskRunner;
		this.resourceFinder = resourceFinder;
		this.publisher = publisher;
	}

	@Override
	public void start() throws Exception {
		taskRunner.execute(new ResourceTask("preloading configuration script") {
			
			@Override
			public void run() {
				ConfigResource config = resourceFinder.loadResource(ConfigResource.class, AppLocation.Base, ConfigResource.CONFIG_JS);
				if (config != null) {
					publisher.publish(new ConfigurationFound(config.path()));
				} else {
					publisher.publish(new UsingDefaultConfiguration());
				}
			}
		});
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
