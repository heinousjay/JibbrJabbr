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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.resource.ResourceFinder;
import jj.resource.ScriptResource;
import jj.resource.SpecResource;

/**
 * @author jason
 *
 */
@Singleton
public class SpecRunner {
	
	private final Logger logger = LoggerFactory.getLogger(SpecRunner.class);

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
	public void runSpecFor(final ScriptResource scriptResource) {
		
		SpecResource specResource = resourceFinder.findResource(SpecResource.class, scriptResource.baseName());
		
		if (specResource != null) {
			
			// make them depend on each other so updates will cause mutual destruction
			
			scriptResource.dependsOn(specResource);
			specResource.dependsOn(scriptResource);
			
			logger.info("running a spec!");
		}
		
	}

}
