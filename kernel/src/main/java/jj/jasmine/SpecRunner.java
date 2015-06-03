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

import static jj.server.ServerLocation.Virtual;
import static jj.jasmine.JasmineScriptEnvironment.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.event.Listener;
import jj.event.Subscriber;
import jj.resource.PathResolver;
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
	private static final String JASMINE_SPEC_JS_ENDING = "-jasmine" + SPEC_JS_ENDING;
	
	private final JasmineConfiguration configuration;
	private final JasmineSwitch jasmineSwitch;
	private final ResourceLoader resourceLoader;
	private final PathResolver pathResolver;
	// make this contributable
	private final Set<String> ignoredNames = new HashSet<>(Arrays.asList(JASMINE_JS, JASMINE_BOOT_JS, JASMINE_RUN_JS));
	
	@Inject
	SpecRunner(
		JasmineConfiguration configuration,
		JasmineSwitch jasmineSwitch,
		ResourceLoader resourceLoader,
		PathResolver pathResolver
	) {
		this.configuration = configuration;
		this.jasmineSwitch = jasmineSwitch;
		this.resourceLoader = resourceLoader;
		this.pathResolver = pathResolver;
	}
	
	private boolean shouldRun() {
		return jasmineSwitch.runAllSpecs() || configuration.autorunSpecs();
	}

	@Listener
	void on(ResourceLoaded rl) {
		if (shouldRun() &&                                               // are we even on?
			ScriptResource.class.isAssignableFrom(rl.resourceClass) &&   // was it a script that got loaded?
			!ignoredNames.contains(rl.name) &&                           // are we not recursively trying to test our runner scripts?
			pathResolver.specLocationFor(rl.base) != null &&             // a spec location exists for this resource
			!rl.name.endsWith(SPEC_JS_ENDING)                            // kill this when spec locations make sense
		) {
			// kick off a resource thread to look for a spec resource, and determine if it's jasmine or mocha or whatevs
			resourceLoader.loadResource(JasmineScriptEnvironment.class, Virtual, rl.name.replace(JS_ENDING, JASMINE_SPEC_JS_ENDING), rl);
		}
	}
}
