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

import static jj.configuration.resolution.AppLocation.Base;
import static jj.configuration.ConfigurationScriptEnvironmentCreator.*;

import java.io.IOException;

import javax.inject.Inject;

import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import jj.event.Listener;
import jj.event.Publisher;
import jj.event.Subscriber;
import jj.resource.NoSuchResourceException;
import jj.resource.ResourceFinder;
import jj.script.AbstractScriptEnvironment;
import jj.script.Global;
import jj.script.ScriptEnvironmentInitialized;
import jj.script.module.RootScriptEnvironment;
import jj.script.module.ScriptResource;

/**
 * <p>
 * Running environment for configuration scripts.
 * 
 * <p>
 * Config scripts are kinda simple, there can be only one.
 * 
 * 
 * @author jason
 *
 */
@Subscriber
class ConfigurationScriptEnvironment extends AbstractScriptEnvironment implements RootScriptEnvironment {
	
	private final Publisher publisher;
	
	private final ScriptableObject global;
	
	private final ScriptableObject scope;
	
	private final ScriptResource config;
	
	private final ConfigurationCollector collector;
	
	@Inject
	ConfigurationScriptEnvironment(
		final Dependencies dependencies,
		final ResourceFinder resourceFinder,
		final Publisher publisher,
		final @Global ScriptableObject global,
		final ConfigurationCollector collector
	) {
		super(dependencies);
		
		this.publisher = publisher;
		
		this.global = global;
		
		this.collector = collector;
		
		scope = createChainedScope(global);
		configureModuleObjects("configuration", scope);
		
		publisher.publish(new ConfigurationLoading());
		
		config = resourceFinder.loadResource(ScriptResource.class, Base, CONFIG_SCRIPT_NAME);
		
		if (config != null) {
			publisher.publish(new ConfigurationFound(config.path()));
			config.addDependent(this);
		} else {
			publisher.publish(new UsingDefaultConfiguration());
			configurationComplete();
			throw new NoSuchResourceException(getClass(), CONFIG_SCRIPT_NAME);
		}
	}
	
	private void configurationComplete() {
		collector.configurationComplete();
		publisher.publish(new ConfigurationLoaded());
	}
	
	@Listener
	void scriptInitialized(final ScriptEnvironmentInitialized event) {
		if (event.scriptEnvironment() == this) {
			configurationComplete();
		}
	}

	@Override
	public Scriptable scope() {
		return scope;
	}

	@Override
	public Script script() {
		return config.script();
	}

	@Override
	public String scriptName() {
		return CONFIG_SCRIPT_NAME;
	}

	@Override
	public String name() {
		return CONFIG_NAME;
	}

	@Override
	public String uri() {
		return "";
	}

	@Override
	public String sha1() {
		return config.sha1();
	}

	@Override
	public boolean needsReplacing() throws IOException {
		// always replaced by the underlying config changing
		return false;
	}

	@Override
	public ScriptableObject global() {
		return global;
	}

	@Override
	protected boolean removeOnReload() { // just get us a new copy
		return false;
	}
}
