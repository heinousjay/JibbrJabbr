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
package jj.http.server;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import jj.script.Global;
import jj.script.RhinoContext;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * <p>
 * Helper to expose/map {@link ServableResource} classes to the routing engine
 * so configuration scripts can use them for routing without specifically hard-coding
 * the actual types.
 * 
 * @author jason
 *
 */
@Singleton
public class ServableResources {

	private final ScriptableObject global;
	private final Provider<RhinoContext> contextProvider;
	private final Map<String, Class<? extends ServableResource>> servableResources;
	
	@Inject
	ServableResources(
		final @Global ScriptableObject global,
		final Provider<RhinoContext> contextProvider,
		final Map<String, Class<? extends ServableResource>> servableResources
	) {
		this.global = global;
		this.contextProvider = contextProvider;
		this.servableResources = servableResources;
	}
	
	// returns a new version every time to prevent e.g. silly extensions grabbing the object
	// and modifying it
	public Scriptable arrayOfNames() {
		try (RhinoContext context = contextProvider.get()) {
			Scriptable result = context.newArray(global, servableResources.size());
			int index = 0;
			for (String name : servableResources.keySet()) {
				result.put(index++, result, name);
			}
			return result;
		}
	}
	
	public Class<? extends ServableResource> classFor(String name) {
		return servableResources.get(name);
	}
}
