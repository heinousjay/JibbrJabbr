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

import jj.client.ClientModule;
import jj.document.DocumentModule;
import jj.hostapi.HostApiModule;
import jj.logging.LoggingModule;
import jj.resource.ResourceModule;
import jj.script.ScriptModule;
import jj.servable.ServableModule;
import jj.webbit.WebbitModule;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * @author jason
 *
 */
public class CoreModule extends AbstractModule {
	
	private final String [] args;
	private final boolean isTest;
	
	public CoreModule(final String [] args, final boolean isTest) {
		this.args = args;
		this.isTest =isTest;
	}

	@Override
	protected void configure() {
		
		Multibinder<JJServerListener> serverListeners = Multibinder.newSetBinder(binder(), JJServerListener.class);
		serverListeners.addBinding().to(LogConfigurator.class);
		serverListeners.addBinding().to(IOExecutor.class);
		serverListeners.addBinding().to(HttpControlExecutor.class);
		
		// this gets instantiated before anything might write to a log
		bind(LogConfigurator.class).toInstance(new LogConfigurator(isTest));
		
		// you want the command line args?  HAVE AT EM
		bind(String[].class).toInstance(args);
		
		// for now.  this will have different installations in different
		// environments, i'd think.  if you replace this binding you'll also
		// have to  do something with TaskCreator
		bind(ExecutionTrace.class).to(ExecutionTraceImpl.class);
		
		// a good place to break apart crafty circular dependencies
		bind(JJExecutors.class).to(JJExecutorsImpl.class);
		
		install(new ClientModule());
		install(new DocumentModule());
		install(new HostApiModule());
		install(new LoggingModule());
		install(new ResourceModule(isTest));
		install(new ScriptModule());
		install(new ServableModule());
		install(new WebbitModule(isTest));
	}

}
