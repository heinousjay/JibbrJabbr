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
package jj;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

import jj.configuration.ConfigurationModule;
import jj.conversion.ConversionModule;
import jj.engine.HostApiModule;
import jj.event.EventModule;
import jj.execution.ExecutionModule;
import jj.jasmine.JasmineModule;
import jj.jjmessage.JJMessageModule;
import jj.logging.LoggingModule;
import jj.resource.ResourceModule;
import jj.script.ScriptModule;
import jj.http.HttpModule;

/**
 * @author jason
 *
 */
public class CoreModule extends JJModule {
	
	// can probably live in RhinoContext, assuming that is the only entry point to the context
	
	// this lives here because we need to set up the context factory
	// before anything actually creates a context, otherwise we won't
	// get the "enhanced java access" which means exceptions will just
	// break on through the scripts
	private static final class JJContextFactory extends ContextFactory {
		
		@Override
		protected boolean hasFeature(Context cx, int featureIndex) {
			return (featureIndex == Context.FEATURE_ENHANCED_JAVA_ACCESS) || super.hasFeature(cx, featureIndex);
		}
	}
	
	static {
		ContextFactory.initGlobal(new JJContextFactory());
	}
	
	private final String [] args;
	private final boolean isTest;
	private final ResourceResolver resourceResolver;
	
	public CoreModule(final String[] args) {
		this(args, null);
	}
	
	public CoreModule(final String [] args, final ResourceResolver resourceResolver) {
		this.args = args;
		this.isTest = resourceResolver == null;
		this.resourceResolver = resourceResolver == null ? new BootstrapClassPath() : resourceResolver;
	}

	@Override
	protected void configure() {
		
		// bind up the command line args
		bind(String[].class).annotatedWith(CommandLine.class).toInstance(args);
		bind(ResourceResolver.class).toInstance(resourceResolver);
		bind(Version.class).to(VersionImpl.class);
		
		// we need the logging module to configure our async logger before we do anything that might log
		install(new LoggingModule(isTest));
		
		// first our key pieces
		install(new ConfigurationModule());
		install(new ConversionModule());
		install(new EventModule());
		install(new ExecutionModule());
		
		// extract the Document system from here.
		// it is a standalone feature
		install(new ResourceModule());
		install(new ScriptModule());
		// everything before here (once the Document system is extracted)
		// can be started with no configuration, and then changed on the fly
		
		// this is second wave, in the new plan - restartable services with bridged
		// access to the core
		install(new HttpModule(isTest));
		
		// this needs to be split into pieces and contributed
		// from places that make the most sense
		// for instance the module system is an intrinsic service
		// of the script feature, so the require function should come
		// from there.  broadcast is provided by the websocket system,
		// so it should come from there.
		install(new HostApiModule());
		
		// this is part of the Document system, and should probably be renamed
		// or at least repackaged
		install(new JJMessageModule());
		
		// this is not a thing yet but it will be!
		install(new JasmineModule());
	}

}
