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
package jj.http.server;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.netty.channel.nio.NioEventLoopGroup;

/**
 * @author jason
 *
 */
@Singleton
class HttpServerNioEventLoopGroup extends NioEventLoopGroup {
	
	private static ExecutorService executorService(final int threads, final UncaughtExceptionHandler uncaughtExceptionHandler) {
		
		return Executors.newFixedThreadPool(threads, new ThreadFactory() {
			
			private final AtomicInteger id = new AtomicInteger();
			
			@Override
			public Thread newThread(Runnable r) {
				
				Thread thread = new Thread(r, "JibbrJabbr HTTP Server I/O Handler " + id.incrementAndGet());
				thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
				return thread;
			}
		});
	}
	
	private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

	@Inject
	HttpServerNioEventLoopGroup(
		final UncaughtExceptionHandler uncaughtExceptionHandler
	) {
		super(THREAD_COUNT, executorService(THREAD_COUNT, uncaughtExceptionHandler));
	}
	
	
}
