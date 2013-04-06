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
		
		bind(Configuration.class).toInstance(new Configuration(args));
		serverListeners.addBinding().toInstance(new LogConfigurator(isTest));
		bind(JJServerLifecycle.class);
		bind(IOExecutor.class);
		bind(HttpControlExecutor.class);
		bind(TaskCreator.class);
		
		// a good place to break apart crafty circular dependencies
		bind(JJExecutors.class).to(JJExecutorsImpl.class);
		
		install(new ClientModule());
		install(new DocumentModule());
		install(new HostApiModule());
		install(new ResourceModule(isTest));
		install(new ScriptModule());
		install(new ServableModule());
		install(new WebbitModule(isTest));
	}

}
