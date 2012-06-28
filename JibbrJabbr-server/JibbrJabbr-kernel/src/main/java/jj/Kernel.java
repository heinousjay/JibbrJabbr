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

import static jj.KernelControl.*;
import static org.picocontainer.Characteristics.NONE;
import static org.jboss.netty.util.ThreadNameDeterminer.CURRENT;

import jj.api.Version;
import jj.html.HTMLFragmentFinder;

import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.jboss.netty.util.ThreadRenamingRunnable;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Puts the server components together and manages their lifecycle.
 * 
 * container hierarchy should be something like
 * 
 * core
 *  - thread pools
 *  - event mediator
 *  - settings
 *  - messaging
 *  - logging
 *  - i/o container
 *    - http/websocket/spdy? handler (netty)
 *    - file watch service
 *    - filesystem service
 *  - application container - uses events to communicate with i/o container, no direct dependencies allowed!
 *    - application loader
 *    - individual application containers?
 *      - responders
 *      
 * each layer uses the services of the layer(s) underneath
 * 
 * @author jason
 *
 */
public class Kernel {
	
	static {
		// this is just a convenience for jason - netty does not yet correctly
		// determine the constraint level for JDK 7 on Mac.
		System.setProperty("org.jboss.netty.channel.socket.nio.constraintLevel", "0");
		
		
		// set netty to log to our logger
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
		// and tell netty to stop renaming our threads
		ThreadRenamingRunnable.setThreadNameDeterminer(CURRENT);
	}
	
	private final Logger logger = LoggerFactory.getLogger(Kernel.class);
	
	/**
	 * The core PicoContainer used to bootstrap the event mediator and
	 * server executors
	 */
	private final MutablePicoContainer coreContainer =
		new DefaultPicoContainer(
			new Caching().wrap(new ConstructorInjection()),
			new NullLifecycleStrategy(),
			null,
			new NullComponentMonitor()
		);
	
	/**
	 * The PicoContainer used to talk to the outside world.  communicates with
	 * the rest of the system entirely via events
	 */
	private final MutablePicoContainer ioContainer =
		new DefaultPicoContainer(
			new Caching().wrap(new ConstructorInjection()),
			new JJLifecycleStrategy(coreContainer),
			coreContainer,
			new JJComponentMonitor()
		);
	
	private final EventMediationService ems;
	
	public Kernel(String[] args, boolean daemon) {
		
		// move this into something else
		logger.info("Welcome to {} {}", Version.name, Version.version);
		
		coreContainer.setName("Kernel Core");
		
		coreContainer.addComponent(args)
					.addComponent(KernelSettings.class)
					.addComponent(EventMediationService.class)
					.addComponent(SynchronousThreadPoolExecutor.class)
					.addComponent(AsynchronousThreadPoolExecutor.class)
					.addAdapter(new MessageConveyorProvidingAdapter())
					.as(NONE).addAdapter(new LocLoggerProvidingAdapter());	 
					 
		coreContainer.addChildContainer(ioContainer);
		
		ioContainer.setName("Kernel I/O");
	
		ioContainer.addComponent(HTMLFragmentFinder.class)
				.addComponent(HttpRequestHandler.class)
				.addComponent(HttpServer.class)
				.addComponent(NettyRequestBridge.class);
		
		coreContainer.start();
		
		ems = coreContainer.getComponent(EventMediationService.class);
		
		if (!daemon) {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					Kernel.this.stop();
					Kernel.this.dispose();
				}
			});
			
			start();
		}
	}
	
	public void start() {
		ems.publish(Start);
	}
	
	public void stop() {
		ems.publish(Stop);
	}
	
	public void dispose() {
		
	}
}
