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

import static org.picocontainer.Characteristics.HIDE_IMPL;
import jj.document.DocumentInitializer;
import jj.hostapi.HostApiInitializer;
import jj.resource.ResourceInitializer;
import jj.script.ScriptInitializer;
import jj.servable.ServableInitializer;
import jj.webbit.WebbitInitializer;

import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.AdaptingBehavior;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.lifecycle.NullLifecycleStrategy;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;

/**
 * @author  jason
 */
public class Startup {
	/**
	 * 
	 */
	private final MutablePicoContainer container;

	/**
	 * 
	 */
	public Startup(final String[] args, final boolean isTest) {
		container = 
			new DefaultPicoContainer(
				new Caching().wrap(new AdaptingBehavior()),
				new NullLifecycleStrategy(),
				null,
				new JJComponentMonitor()
			)
			.addComponent(new Configuration(args))
			.addComponent(new SLF4JConfiguration())
			
			.addComponent(JJServerLifecycle.class)
			.addComponent(IOExecutor.class)
			.addComponent(HttpControlExecutor.class)
			
			// a good place to break apart crafty circular dependencies
			.as(HIDE_IMPL).addComponent(JJExecutors.class, JJExecutorsImpl.class)

			// needs to be smarter configuration? i at least should be
			// supplying the executor
			.addComponent(new AsyncHttpClientConfig.Builder()
				.setCompressionEnabled(true)
				.setUserAgent("JibbrJabbr RestCall subsystem/Netty 3.5.11Final")
				.setIOThreadMultiplier(1)
				.setFollowRedirects(true)
				.build()
			)
			.addComponent(AsyncHttpClient.class);
		
		ServableInitializer.initialize(container);
		DocumentInitializer.initialize(container);
		ScriptInitializer.initialize(container);
		ResourceInitializer.initialize(container);
		HostApiInitializer.initialize(container);
		WebbitInitializer.initialize(container, isTest);
	}
	
	public MutablePicoContainer container() {
		return container;
	}
}