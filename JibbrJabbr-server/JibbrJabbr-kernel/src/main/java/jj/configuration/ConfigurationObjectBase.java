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
import java.util.concurrent.atomic.AtomicReference;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import jj.CoreConfiguration;
import jj.conversion.Converters;
import jj.resource.ConfigResource;
import jj.resource.ResourceFinder;
import jj.script.RhinoContext;
import jj.script.RhinoContextMaker;

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
	
	private final Arguments arguments;
	
	private final Converters converters;
	
	private final Scriptable scriptObject;
	
	private final ResourceFinder resourceFinder;
	
	private final AtomicReference<Function> function = new AtomicReference<>();
	
	protected final RhinoContextMaker contextMaker;
	
	protected final Map<String, Object[]> values = new HashMap<>();
	
	protected ConfigurationObjectBase(
		final Arguments arguments,
		final Converters converters,
		final ResourceFinder resourceFinder,
		final RhinoContextMaker rhinoContextMaker
	) {
		this.arguments = arguments;
		this.converters = converters;
		this.resourceFinder = resourceFinder;
		this.contextMaker = rhinoContextMaker;
		// special case, the core configuration CANNOT have script
		this.scriptObject = (this instanceof CoreConfiguration) ? null : configureScriptObject(configResource().global());
	}
	
	final String name() {
		String base = getClass().getSimpleName().replace("Configuration", "");
		return base.substring(0, 1).toLowerCase() + base.substring(1);
	}
	
	
	protected abstract Scriptable configureScriptObject(Scriptable scope);
	
	protected final Function configurationFunction(String name) {
		return new ConfigurationFunction(values, name);
	}
	
	protected final ConfigResource configResource() {
		return resourceFinder.findResource(ConfigResource.class, ConfigResource.CONFIG_JS);
	}
	
	protected final <T> T readArgument(String name, String defaultValue, Class<T> resultClass) {
		String value = arguments.get(name);
		if (value == null) {
			value = defaultValue;
		}
		if (value != null) {
			return converters.convert(value, resultClass);
		}
		
		return null;
	}
	
	void runScriptFunction() {
		
		ConfigResource config = configResource();
		if (config != null && function.get() != config.functions().get(name())) {
			try (RhinoContext context = contextMaker.context()) {
				Function function = config.functions().get(name());
				context.callFunction(function, config.global(), config.global(), new Object[] {scriptObject});
			}
		}
	}
	
	protected final <T> T readScriptValue(String name, String defaultValue, Class<T> resultClass) {
		Object value = values.get(name)[0];
		if (value == null) {
			value = defaultValue;
		}
		if (value != null) {
			return converters.convert(value, resultClass);
		}
		
		return null;
	}
}
