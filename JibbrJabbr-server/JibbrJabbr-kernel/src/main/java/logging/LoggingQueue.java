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
package logging;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.LoggerFactory;

import net.jcip.annotations.ThreadSafe;

import jj.NonBlocking;
import jj.SynchThreadPool;

/**
 * Simple backbone for non-blocking logging.  Dump messages on the queue and they
 * get logged in another thread.
 * 
 * @author jason
 *
 */
@ThreadSafe
public class LoggingQueue {

	private volatile boolean run = true;
	
	private final LinkedBlockingQueue<LogMessage> queue = new LinkedBlockingQueue<>();
	
	@NonBlocking
	public LoggingQueue(final SynchThreadPool executor) {
		executor.submit(new Runnable() {
			
			@Override
			public void run() {
				try {
					while (run) {
						LogMessage message = queue.take();
						message.type.log(LoggerFactory.getLogger(message.loggerName), message.marker, message.message, message.args);
					}
				} catch (InterruptedException e) {
					run = false; // hardly matters
					// reset the interrupt status, might not be needed but hardly hurts
					Thread.currentThread().interrupt();
				}
			}
		});
	}
	
	@NonBlocking
	public void offer(LogMessage message) {
		// technically might need a retry here but
		// we are unbounded so it will only fail if
		// we run out of memory in which case.. whatever.
		// we can safely drop logging on the floor at that point
		queue.offer(message);
	}
	
	@NonBlocking
	public void shutdown() {
		run = false;
	}
}
