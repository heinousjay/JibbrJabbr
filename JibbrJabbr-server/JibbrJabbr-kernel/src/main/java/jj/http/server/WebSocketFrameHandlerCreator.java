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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import jj.script.AssociatedScriptBundle;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

/**
 * @author jason
 *
 */
@Singleton
class WebSocketFrameHandlerCreator {
	
	private final class CreatorModule extends AbstractModule {
		
		final WebSocketServerHandshaker handshaker;
		
		final AssociatedScriptBundle scriptBundle;
		
		CreatorModule(
			final WebSocketServerHandshaker handshaker,
			final AssociatedScriptBundle scriptBundle
		) {
			this.handshaker = handshaker;
			this.scriptBundle = scriptBundle;
		}

		@Override
		protected void configure() {
			bind(JJWebSocketConnection.class);
			bind(WebSocketFrameHandler.class);
			bind(WebSocketServerHandshaker.class).toInstance(handshaker);
			bind(AssociatedScriptBundle.class).toInstance(scriptBundle);
			
		}
	}
	
	private final Injector parentInjector;
	
	@Inject
	WebSocketFrameHandlerCreator(final Injector parentInjector) {
		this.parentInjector = parentInjector;
	}
	
	WebSocketFrameHandler createHandler(
		final WebSocketServerHandshaker handshaker,
		final AssociatedScriptBundle scriptBundle
	) {
		return parentInjector.createChildInjector(
			new CreatorModule(handshaker, scriptBundle)
		).getInstance(WebSocketFrameHandler.class);
	}

}
