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

import static org.picocontainer.Characteristics.NONE;
import static org.jboss.netty.util.ThreadNameDeterminer.CURRENT;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import jj.api.Version;

import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.jboss.netty.util.ThreadRenamingRunnable;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.monitors.NullComponentMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Puts the server components together and manages their lifecycle.
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
	 * Injected into kernel objects so they can be controlled in arbitrary
	 * fashion by the kernel object.
	 * 
	 * This design is only vaguely testable, which is worrisome.
	 * @author Jason Miller
	 *
	 */
	final class Controller {

		private final boolean daemon;
		
		private Controller(final boolean daemon) {
			this.daemon = daemon;
		}
		
		// synchronizing server start
		// sequence is
		// - start HttpServer initialization thread (Kernel)
		// - awaitHttpSocketBound
		// - bind socket (HttpServer)
		// - awaitHttpServerStart
		//   at this point, drop privileges in the outside daemon code
		// - notifyHttpServerStarted
		private final ReentrantLock serverStartGate = new ReentrantLock();
		private final Condition socketBound = serverStartGate.newCondition();
		private final Condition serverStarted = serverStartGate.newCondition();
		private volatile boolean socketBoundFlag = false; 
		private volatile boolean serverStartedFlag = false; 
		private void awaitHttpSocketBound() {
			if (daemon) {
				serverStartGate.lock();
				try {
					while (!socketBoundFlag) {
						logger.info("awaiting socket bound");
						socketBound.awaitUninterruptibly();
					}
					logger.info("socket bound notification delivered");
				} finally {
					serverStartGate.unlock();
				}
			}
		}
		void awaitHttpServerStart() {
			if (daemon) {
				serverStartGate.lock();
				try {
					socketBoundFlag = true;
					logger.info("notifying socket bound");
					socketBound.signal();
					while (!serverStartedFlag) {
						logger.info("awaiting server start");
						serverStarted.awaitUninterruptibly();
					}
					logger.info("server start notification delivered");
				} finally {
					serverStartGate.unlock();
				}
			}
		}
		private void notifyHttpServerStarted() {
			if (daemon) {
				serverStartGate.lock();
				try {
					serverStartedFlag = true;
					logger.info("notifying server started");
					serverStarted.signal();
				} finally {
					serverStartGate.unlock();
				}
			}
		}
		
		private volatile boolean clearToServe = false;
		public boolean clearToServe() {
			return clearToServe;
		}
	}
	
	/**
	 * Defines how the kernel manages its lifecycle, 
	 * depending on how it was invoked
	 * @author Jason Miller
	 *
	 */
	private interface KernelLifecycleStrategy {
		
		void init();
		void start();
		void stop();
		void dispose();
	}
	
	/**
	 * Kernel lifecycle when we are our own process
	 * @author Jason Miller
	 *
	 */
	private final class ProcessLifecycle
			implements KernelLifecycleStrategy {
		
		@Override
		public void init() {
			logger.info("Process.init");
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					// this is to be split up later, when there is a way
					// of externally controlling the kernel lifecycle.
					// for now it can just stay in here
					ProcessLifecycle.this.stop();
					ProcessLifecycle.this.dispose();
				}
			});
			coreContainer.start();
			logger.info("Process.init complete");
		}

		@Override
		public void start() {
			sync.clearToServe = true;
		}

		@Override
		public void stop() {
			sync.clearToServe = false;
		}

		@Override
		public void dispose() {
			coreContainer.dispose();
		}
		
	}
	
	private final class DaemonLifecycle
			implements KernelLifecycleStrategy {
		
		@Override
		public void init() {
			logger.info("Daemon.init");
			
			new Thread("kernel initialization helper") {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					// ugly mess of a thing, this is...
					// gonna have to extract the lifecycle stuff,
					// picocontainer keeps making me sad.
					coreContainer.start();
				}
			}.start();
			
			sync.awaitHttpSocketBound();
			logger.info("Daemon.init complete");
		}

		@Override
		public void start() {
			sync.notifyHttpServerStarted();
			sync.clearToServe = true;
		}

		@Override
		public void stop() {
			sync.clearToServe = false;
		}

		@Override
		public void dispose() {
			coreContainer.dispose();
		}
		
	}
	
	
	private final Controller sync;
	
	private final KernelLifecycleStrategy lifecycle;
	
	/**
	 * The core PicoContainer used to hold the most basic server
	 * objects.
	 */
	private final MutablePicoContainer coreContainer =
		new DefaultPicoContainer(
			new Caching().wrap(new ConstructorInjection()),
			new JJLifecycleStrategy(),
			null,
			new NullComponentMonitor()
		);

	public Kernel(String[] args, boolean daemon) {

		logger.info("Welcome to {} {}", Version.name, Version.version);

		sync = new Controller(daemon);
		
		lifecycle = daemon ? new DaemonLifecycle() : new ProcessLifecycle();
		
		coreContainer.setName("Kernel");
		
		coreContainer.addComponent(sync)
					 .addComponent(args)
					 .addComponent(KernelSettings.class)
					 .addComponent(HttpServer.class)
					 .addComponent(NettyRequestBridge.class)
					 .addComponent(KernelThreadPoolExecutor.class)
					 .addComponent(HttpThreadPoolExecutor.class)
					 .addAdapter(new MessageConveyorProvidingAdapter())
					 .as(NONE).addAdapter(new LocLoggerProvidingAdapter());

		lifecycle.init();
	}
	
	public void start() {
		lifecycle.start();
	}
	
	public void stop() {
		lifecycle.stop();
	}
	
	public void dispose() {
		lifecycle.dispose();
	}
}
