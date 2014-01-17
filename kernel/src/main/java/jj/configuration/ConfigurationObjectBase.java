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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import jj.conversion.Converters;
import jj.resource.ResourceFinder;
import jj.resource.config.ConfigResource;
import jj.script.RhinoContext;

/**
 * <p>
 * mediates between the generated configuration object
 * and the rest of the system.
 * </p>
 * <p>
 * has to be public so the internal classloader can see it,
 * but nothing should descend from this class statically
 * </p>
 * @author jason
 *
 */
public abstract class ConfigurationObjectBase {
	
	protected final Converters converters;
	
	private final Scriptable scriptObject;
	
	private final ResourceFinder resourceFinder;
	
	private final String name;
	
	protected final Provider<RhinoContext> contextProvider;
	
	protected final Map<String, Object> values = new HashMap<>();
	
	protected ConfigurationObjectBase(
		final Converters converters,
		final ResourceFinder resourceFinder,
		final Provider<RhinoContext> contextProvider
	) {
		this.converters = converters;
		this.resourceFinder = resourceFinder;
		this.contextProvider = contextProvider;
		
		ConfigResource configResource = configResource();
		
		this.scriptObject = configResource == null ? null : configureScriptObject(configResource.global());
		
		Class<?>[] interfaces = getClass().getInterfaces();
		assert interfaces.length == 1 : "there can be only one! (interface per configuration object)";
		String base = interfaces[0].getSimpleName().replace("Configuration", "");
		name = base.substring(0, 1).toLowerCase() + base.substring(1);
	}
	
	final String name() {
		return name;
	}
	
	
	protected abstract Scriptable configureScriptObject(Scriptable scope);
	
	protected abstract void setDefaults();
	
	protected final Function configurationFunction(String name, Class<?> returnType, boolean allArgs, String defaultValue) {
		return new ConfigurationFunction(converters, values, name, returnType, allArgs, defaultValue);
	}
	
	protected final ConfigResource configResource() {
		return resourceFinder.findResource(ConfigResource.class, ConfigResource.CONFIG_JS);
	}
	
	void runScriptFunction() {
		ConfigResource config = configResource();
		if (config != null && config.functions().containsKey(name())) {
			try (RhinoContext context = contextProvider.get()) {
				Function function = config.functions().get(name());
				context.callFunction(function, config.global(), config.global(), new Object[] {scriptObject});
			}
		} else {
			setDefaults();
		}
	}
}
