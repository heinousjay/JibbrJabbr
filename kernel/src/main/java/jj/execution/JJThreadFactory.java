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
package jj.execution;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

/**
 * @author jason
 *
 */
public class JJThreadFactory implements ThreadFactory {
	
	private final ThreadLocal<Boolean> flag = new ThreadLocal<>();
	
	private final UncaughtExceptionHandler uncaughtExceptionHandler;
	
	private final AtomicInteger id = new AtomicInteger();
	
	private String namePattern;

	/**
	 * @param uncaughtExceptionHandler
	 */
	@Inject
	public JJThreadFactory(UncaughtExceptionHandler uncaughtExceptionHandler) {
		this.uncaughtExceptionHandler = uncaughtExceptionHandler;
	}
	
	public JJThreadFactory namePattern(String namePattern) {
		this.namePattern = namePattern;
		return this;
	}

	@Override
	public Thread newThread(final Runnable r) {
		final String name = String.format(
			namePattern, 
			id.incrementAndGet()
		);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				flag.set(Boolean.TRUE);
				r.run();
				flag.set(null);
			}
		}, name);
		thread.setDaemon(true);
		thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
		return thread;
	}
	
	public boolean in() {
		return flag.get() == Boolean.TRUE;
	}
}