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

import jj.client.ClientModule;
import jj.document.DocumentModule;
import jj.hostapi.HostApiModule;
import jj.logging.LoggingModule;
import jj.resource.ResourceModule;
import jj.script.ScriptModule;
import jj.servable.ServableModule;
import jj.http.WebbitModule;

/**
 * @author jason
 *
 */
public class CoreModule extends JJModule {
	
	// this stuff lives here because we need to set up the context factory
	// before anything actually creates a context, otherwise we won't
	// get the "enhanced java access" which means exceptions will just
	// break on through the scripts
	private static final class JJContext extends Context {
		JJContext(final ContextFactory factory) {
			super(factory);
		}
	}
	
	private static final class JJContextFactory extends ContextFactory {
		
		@Override
		protected Context makeContext() {
			Context context = new JJContext(this);
			return context;
		}
		
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
		
		// we need the logging module to configure our async logger before we do anything that might log
		install(new LoggingModule(isTest));
		
		addServerListenerBinding().to(IOExecutor.class);
		addServerListenerBinding().to(HttpControlExecutor.class);
		addServerListenerBinding().to(ScriptExecutorFactory.class);
		addServerListenerBinding().to(ClientExecutor.class);
		
		// you want the command line args?  HAVE AT EM
		bind(String[].class).toInstance(args);
		
		// for now.  this will have different installations in different
		// environments, i'd think.  if you replace this binding you'll also
		// have to  do something with TaskCreator
		bind(ExecutionTrace.class).to(ExecutionTraceImpl.class);
		
		// a good place to break apart crafty circular dependencies.  this is
		// the most popular object in the system.  for good reason.
		bind(JJExecutors.class).to(JJExecutorsImpl.class);
		
		// and install our little pieces
		install(new ClientModule());
		install(new DocumentModule());
		install(new HostApiModule());
		install(new ResourceModule(isTest));
		install(new ScriptModule());
		install(new ServableModule());
		install(new WebbitModule(isTest));
	}

}
