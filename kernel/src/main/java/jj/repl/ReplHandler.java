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
package jj.repl;

import static jj.server.ServerLocation.Virtual;

import javax.inject.Inject;
import javax.inject.Provider;

import org.mozilla.javascript.Script;

import jj.event.Listener;
import jj.event.Subscriber;
import jj.execution.TaskRunner;
import jj.resource.ResourceFinder;
import jj.resource.ResourceReloaded;
import jj.script.RhinoContext;
import jj.script.ScriptTask;
import jj.util.Closer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * The handler for the REPL if we're in linemode. this can happen if,
 * for instance, the client refuses to leave linemode (no idea why)
 * or if the user chooses netcat to connect, which doesn't understand telnet
 * stuff.
 * 
 * @author jason
 *
 */
@Subscriber
class ReplHandler extends SimpleChannelInboundHandler<String> {
	
	private final ResourceFinder resourceFinder;
	private final TaskRunner taskRunner;
	private final CurrentReplChannelHandlerContext currentCtx;
	private final Provider<RhinoContext> contextProvider;
	
	private ChannelHandlerContext ctx;
	
	@Inject
	ReplHandler(
		final ResourceFinder resourceFinder,
		final TaskRunner taskRunner,
		final CurrentReplChannelHandlerContext currentCtx,
		final Provider<RhinoContext> contextProvider
	) {
		this.resourceFinder = resourceFinder;
		this.taskRunner = taskRunner;
		this.currentCtx = currentCtx;
		this.contextProvider = contextProvider;
	}
	
	@Listener
	void resourceReloaded(ResourceReloaded resourceReloaded) {
		if (resourceReloaded.resourceClass == ReplScriptEnvironment.class) {
			ctx.writeAndFlush("\nreloading! your definitions are gone\n>");
		}
	}
	
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		
		this.ctx = ctx;
		
		ctx.writeAndFlush("Welcome to JibbrJabbr\n>");
	}

	@Override
	protected void messageReceived(final ChannelHandlerContext ctx, final String msg) throws Exception {
		
		ReplScriptEnvironment rse = 
			resourceFinder.findResource(ReplScriptEnvironment.class, Virtual, ReplScriptEnvironment.NAME);
		
		assert rse != null && rse.alive() : "no ReplScriptEnvironment found!";
		
		if (msg.trim().isEmpty()) {
			
			ctx.writeAndFlush(">");
			
		} else {
		
			try (RhinoContext context = contextProvider.get()) {
				
				final Script script = context.compileString(
					"$$print(function() { return " + msg + "; });",
					"repl-console"
				);
	
				taskRunner.execute(new ScriptTask<ReplScriptEnvironment>("repl execution:\n" + msg, rse) {
	
					@Override
					protected void begin() throws Exception {
						
						try (Closer closer = currentCtx.enterScope(ctx)) {
							pendingKey = scriptEnvironment.execute(script);
						}
					}
				});
				
			} catch (Exception e) {
				
				ctx.writeAndFlush(e.getMessage() + "\n>");
			}
		}
	}
}
