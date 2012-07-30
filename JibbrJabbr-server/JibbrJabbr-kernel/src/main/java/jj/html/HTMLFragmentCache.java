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
package jj.html;

import static jj.KernelMessages.LoopThreadName;
import static jj.KernelMessages.ObjectInstantiating;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedTransferQueue;

import org.slf4j.cal10n.LocLogger;

import ch.qos.cal10n.MessageConveyor;

import jj.KernelControl;
import jj.KernelTask;
import jj.SynchThreadPool;
import jj.io.FileBytesRetriever;

/**
 * Almost certainly needs to be broken into an abstract cache and this
 * 
 * new plan - almost certainly is the wrong place for this and the module
 * system will handle caching
 * 
 * @author jason
 *
 */
public class HTMLFragmentCache {

	private static WeakReference<HTMLFragmentCache> instance = null;
	
	static void offer(HTMLFragmentFinder htmlFragmentFinder) {
		HTMLFragmentCache htmlFragmentCache = instance.get();
		if (htmlFragmentCache != null) {
			htmlFragmentCache.requestQueue.offer(htmlFragmentFinder);
		}
	}
	
	private final LinkedTransferQueue<HTMLFragmentFinder> requestQueue = new LinkedTransferQueue<>();
	
	// maybe soft references?  that's something to deal with later though.
	// sort of a tuning tradeoff - if memory is getting tight, just look it up all the time?
	private final ConcurrentHashMap<URI, HTMLFragment> cache = new ConcurrentHashMap<>();
	
	private volatile boolean run = true;
	
	public HTMLFragmentCache(
		final SynchThreadPool synchThreadPool,
		final LocLogger logger,
		final MessageConveyor messages
	) {
		
		logger.trace(ObjectInstantiating, HTMLFragmentCache.class.getSimpleName());
		
		synchThreadPool.submit(new Worker(messages.getMessage(LoopThreadName, HTMLFragmentCache.class.getSimpleName())));
		
		instance = new WeakReference<>(this);
	}
	
	public void control(KernelControl control) {
		run = (control != KernelControl.Dispose);
	}
	
	private final class Worker extends KernelTask {
		
		Worker(final String name) {
			super(name);
		}
		
		@Override
		protected void execute() throws Exception {
			while (run) {
				
				final HTMLFragmentFinder request = requestQueue.take();
				
				if (cache.containsValue(request.uri)) {
					// in this thread?  probably not
					request.htmlFragment(cache.get(request.uri));
				} else {
					// this is not a blocking operation
					// although it may be long-running
					// for now, yes, in this thread
					new FileBytesRetriever(request.uri) {
						
						@Override
						protected void failed(Throwable t) {
							request.failed(t);
						}
						
						@Override
						protected void bytes(ByteBuffer bytes) {
							
							// on success, watch this file and update the cache if it changes
							// need to save the charset then!
							HTMLFragment htmlFragment = new HTMLFragment(request.charset.decode(bytes).toString());
							// cache the instance unconditionally here.
							cache.put(request.uri, htmlFragment);
							request.htmlFragment(htmlFragment);
						}
					};
				}
			}
		}
	}
}
