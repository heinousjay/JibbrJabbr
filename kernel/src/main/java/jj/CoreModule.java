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


import javax.inject.Singleton;

import jj.application.ApplicationModule;
import jj.configuration.CommandLine;
import jj.configuration.ConfigurationModule;
import jj.conversion.ConversionModule;
import jj.css.CssModule;
import jj.document.DocumentProcessingModule;
import jj.engine.HostApiModule;
import jj.event.EventModule;
import jj.execution.ExecutionModule;
import jj.i18n.I18NModule;
import jj.jasmine.JasmineModule;
import jj.jjmessage.JJMessageModule;
import jj.logging.BindsLogger;
import jj.logging.LoggingModule;
import jj.repl.ReplModule;
import jj.resource.ResourceModule;
import jj.script.ScriptModule;
import jj.server.ServerModule;
import jj.http.HttpModule;

import java.time.Clock;

/**
 * @author jason
 *
 */
public class CoreModule extends JJModule implements BindsLogger {
	
	private final String [] args;
	private final ResourceResolver resourceResolver;
	
	public CoreModule(final String[] args) {
		this(args, null);
	}
	
	public CoreModule(final String [] args, final ResourceResolver resourceResolver) {
		this.args = args;
		this.resourceResolver = resourceResolver == null ? new BootstrapClassPath() : resourceResolver;
	}

	@Override
	protected void configure() {
		
		CreationScope creationScope = new CreationScope();
		bindScope(CreationScoped.class, creationScope);
		bind(CreationScope.class).toInstance(creationScope);
		
		// we need the logging module to configure our async logger before we do anything that might log
		// this is no longer true! but who cares!
		install(new LoggingModule());
		
		// bind the command line args
		bind(String[].class).annotatedWith(CommandLine.class).toInstance(args);
		
		// bind the given resource resolver
		bind(ResourceResolver.class).toInstance(resourceResolver);
		
		// make sure the version is a singleton
		bind(Version.class).in(Singleton.class);

		// bind a time source
		// TODO should take a time zone from the command line?
		bind(Clock.class).toInstance(Clock.systemUTC());
		
		bindLoggedEventsAnnotatedWith(ServerLogger.class).toLogger(ServerLogger.NAME);
		
		// first our key pieces
		install(new ServerModule());
		install(new ApplicationModule());
		install(new ConfigurationModule());
		install(new ConversionModule());
		install(new EventModule());
		install(new ExecutionModule());
		install(new ResourceModule());
		install(new ScriptModule());
		install(new ReplModule());
		
		install(new HttpModule());
		
		install(new I18NModule());

		install(new DocumentProcessingModule());
		
		// this needs to be split into pieces and contributed
		// from places that make the most sense
		// for instance the module system is an intrinsic service
		// of the script feature, so the require function should come
		// from there.  broadcast is provided by the websocket system,
		// so it should come from there.
		// hey, i did those!
		install(new HostApiModule());
		
		// this is part of the Document system, and should probably be renamed
		// or at least repackaged
		install(new JJMessageModule());
		
		// this is not a thing yet but it will be!
		install(new JasmineModule());
		
		install(new CssModule());
	}

}
