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

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.JJServerStartupListener;
import jj.configuration.resolution.AppLocation;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.resource.ResourceFinder;
import jj.script.ScriptEnvironmentInitialized;
import jj.script.resource.ScriptResource;

/**
 * @author jason
 *
 */
@Singleton
@Subscriber
public class SpecRunner implements JJServerStartupListener {

	private final ResourceFinder resourceFinder;
	
	/**
	 * 
	 */
	@Inject
	SpecRunner(final ResourceFinder resourceFinder) {
		this.resourceFinder = resourceFinder;
	}
	
	/**
	 * It is assumed that the spec is loaded when the original script is loaded
	 * @param scriptResource
	 */
	@Listener
	void findAndExecuteSpec(final ScriptEnvironmentInitialized event) {
		String name = event.scriptEnvironment().scriptName();
		ScriptResource scriptResource = resourceFinder.findResource(ScriptResource.class, AppLocation.Base, name);
		SpecResource specResource = resourceFinder.findResource(SpecResource.class, AppLocation.Base, name);
		
		if (specResource != null) {
			
			// make them depend on each other so updates will cause mutual destruction
			
			scriptResource.addDependent(specResource);
			specResource.addDependent(scriptResource);
		}
	}

	@Override
	public void start() throws Exception {
		// really just to make sure we get created, otherwise nothing
		// talks to us :D
	}

	@Override
	public Priority startPriority() {
		// doesn't really matter
		return Priority.NearHighest;
	}
}
