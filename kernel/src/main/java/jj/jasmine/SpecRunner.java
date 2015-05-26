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
package jj.jasmine;

import static jj.system.ServerLocation.Virtual;
import static jj.jasmine.JasmineScriptEnvironment.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.event.Listener;
import jj.event.Subscriber;
import jj.resource.ResourceLoaded;
import jj.resource.ResourceLoader;
import jj.script.module.ScriptResource;

/**
 * Watches for script resources to be loaded and tries to create
 * JasmineScriptEnvironments for them
 * @author jason
 *
 */
@Singleton
@Subscriber
class SpecRunner {
	
	private static final String JS_ENDING = ".js";
	private static final String SPEC_JS_ENDING = "-spec.js";
	
	private final JasmineConfiguration configuration;
	private final JasmineSwitch jasmineSwitch;
	private final ResourceLoader resourceLoader;
	
	private final Set<String> ignoredNames = new HashSet<>(Arrays.asList(JASMINE_JS, JASMINE_BOOT_JS, JASMINE_RUN_JS));
	
	@Inject
	SpecRunner(
		final JasmineConfiguration configuration,
		final JasmineSwitch jasmineSwitch,
		final ResourceLoader resourceLoader
	) {
		this.configuration = configuration;
		this.jasmineSwitch = jasmineSwitch;
		this.resourceLoader = resourceLoader;
	}
	
	private boolean shouldRun() {
		return jasmineSwitch.runAllSpecs() || configuration.autorunSpecs();
	}

	@Listener
	void resourceLoaded(ResourceLoaded rl) {
		if (shouldRun() &&                                               // are we even on?
			ScriptResource.class.isAssignableFrom(rl.resourceClass) &&   // was it a script that got loaded?
			!ignoredNames.contains(rl.name) &&                           // are we not recursively trying to test our runner scripts?
			!rl.name.endsWith(SPEC_JS_ENDING)                            // and of course, specs don't get specs.  stay out of rabbit holes
		) {
			resourceLoader.loadResource(JasmineScriptEnvironment.class, Virtual, rl.name.replace(JS_ENDING, SPEC_JS_ENDING), rl);
		}
	}
}
