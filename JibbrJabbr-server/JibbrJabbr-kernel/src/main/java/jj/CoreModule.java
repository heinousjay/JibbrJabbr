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
import jj.logging.LoggingModule;
import jj.resource.ResourceModule;
import jj.script.ScriptModule;
import jj.http.HttpModule;

/**
 * @author jason
 *
 */
public class CoreModule extends JJModule {
	
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
	
	public CoreModule(final String [] args, final boolean isTest) {
		this.args = args;
		this.isTest =isTest;
	}

	@Override
	protected void configure() {
		
		// bind up the command line args
		bind(String[].class).toInstance(args);
		
		// we need the logging module to configure our async logger before we do anything that might log
		install(new LoggingModule(isTest));
		
		// and install our little pieces
		install(new ConfigurationModule());
		install(new ConversionModule());
		install(new EventModule());
		install(new ExecutionModule());
		install(new HostApiModule());
		install(new HttpModule(isTest));
		install(new ResourceModule(isTest));
		install(new ScriptModule());
	}

}
