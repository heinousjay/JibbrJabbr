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
package jj.io;

import static jj.KernelMessages.LoopThreadName;
import static jj.KernelMessages.ObjectDisposing;
import static jj.KernelMessages.ObjectInstantiated;

import java.lang.ref.WeakReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedTransferQueue;

import jj.KernelControl;
import jj.KernelTask;
import jj.SynchThreadPool;

import org.slf4j.cal10n.LocLogger;

import ch.qos.cal10n.MessageConveyor;

/**
 * <p>
 * Coordinates system interactions with the local file system.  All interfaces identify
 * files using URIs.  The JDK7 NIO file packages are used, so jars are handled uniformly.
 * </p>
 * 
 * @author jason
 *
 */
public class FileSystemService {
	
	/**
	 * Slightly ugly technique - exposing the instance of the service so the API works,
	 * without strongly keeping it alive
	 * 
	 * there might be a better way, but i think this works
	 */
	private static WeakReference<FileSystemService> fileSystemServiceRef = null;
	
	/**
	 * Helper to get a hold of the FileSystemService instance
	 * @return
	 */
	private static FileSystemService instance() {
		if (fileSystemServiceRef != null) {
			return fileSystemServiceRef.get();
		}
		return null;
	}
	
	static abstract class FileSystemAPI {
		
		volatile boolean active = true;
		
		FileSystemAPI() {
			assert instance() != null : "FileSystemService must be running to use this object";
		}
		
		/**
		 * Called if the operation failed.  Optional operation, but it's probably not something you
		 * want to leave hanging unless you like mysterious stoppage.
		 */
		protected void failed(final Throwable t) {}
		
		abstract void execute();
		
		/**
		 * Attempts to stop processing this task.  In general this cannot be guaranteed to have any effect
		 * since the processing is asynchronous, but if the service didn't get to it yet, then it will be
		 * dropped on the floor
		 */
		public final void cancel() {
			active = false;
		}
		
		/**
		 * Offer the instance to the FileSystemService for processing.
		 */
		final void offer() {
			FileSystemService instance = instance();
			if (instance != null) {
				instance.requestQueue.offer(this);
			}
		}
	}
	
	/**
	 * The queue that feeds the event loop 
	 */
	private final BlockingQueue<FileSystemAPI> requestQueue = new LinkedTransferQueue<>();
	
	private final LocLogger logger;
	
	private volatile boolean run = true;
	
	public FileSystemService(
		final SynchThreadPool synchThreadPool,
		final LocLogger logger,
		final MessageConveyor messages
	) {
		// 
		assert synchThreadPool != null : "No synchronous thread pool provided";
		assert logger != null : "No logger provided";
		assert messages != null : "No messages provided";
		
		this.logger = logger;
		
		synchThreadPool.submit(new Worker(messages.getMessage(LoopThreadName, FileSystemService.class.getSimpleName())));
		
		fileSystemServiceRef = new WeakReference<FileSystemService>(this);
		
		logger.trace(ObjectInstantiated, FileSystemService.class.getSimpleName());
	}
	
	private final ConcurrentMap<Thread, Boolean> threadSet = new ConcurrentHashMap<>();
	
	public void control(final KernelControl control) {
		run = (control != KernelControl.Dispose);
		for (final Thread thread : threadSet.keySet()) {
			thread.interrupt();
		}
	}
	
	private final class Worker extends KernelTask {
		
		Worker(String name) {
			super(name);
		}
		
		@Override
		protected void execute() throws Exception {
			threadSet.put(Thread.currentThread(), true);
			while (run) {
				FileSystemAPI request = requestQueue.take();
				if (run && request.active) request.execute();
			}
		}
		
		/* (non-Javadoc)
		 * @see jj.KernelTask#cleanup()
		 */
		@Override
		protected void cleanup() {
			threadSet.remove(Thread.currentThread());
			logger.trace(ObjectDisposing, FileSystemService.class.getSimpleName());
		}
	};
}
