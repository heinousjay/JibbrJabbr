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
package jj.http;

import java.util.concurrent.ThreadFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.netty.channel.nio.NioEventLoopGroup;

/**
 * @author jason
 *
 */
@Singleton
class JJNioEventLoopGroup extends NioEventLoopGroup {
	
	private static final ThreadFactory threadFactory = new ThreadFactory() {
		
		@Override
		public Thread newThread(Runnable r) {
			
			return new Thread(r, "Thread name!");
		}
	};

	@Inject
	JJNioEventLoopGroup() {
		super(Runtime.getRuntime().availableProcessors() * 2, threadFactory);
	}
}
