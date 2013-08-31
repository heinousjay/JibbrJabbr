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
package jj.script;


import javax.inject.Inject;
import javax.inject.Singleton;

import jj.logging.EmergencyLogger;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;

/**
 * source of rhino contexts.  wrapped for testability
 * @author jason
 *
 */
@Singleton
public class RhinoContextMaker {
	
	private final Logger logger;
	
	private final ScriptableObject generalScope;

	@Inject
	RhinoContextMaker(final @EmergencyLogger Logger logger) {
		this.logger = logger;
		try (RhinoContext context = new RhinoContext(Context.enter(), logger)) {
			generalScope = context.initStandardObjects(true);
		}
	}

	public RhinoContext context() {
		return new RhinoContext(Context.enter(), logger);
	}
	
	/**
	 * A sealed empty global scope that can be used for throwaway executions
	 * @return
	 */
	public ScriptableObject generalScope() {
		return generalScope;
	}
}
