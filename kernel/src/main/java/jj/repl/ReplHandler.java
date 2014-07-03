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

import static jj.configuration.resolution.AppLocation.Virtual;

import javax.inject.Inject;
import javax.inject.Provider;

import org.mozilla.javascript.Script;

import jj.execution.TaskRunner;
import jj.resource.ResourceFinder;
import jj.script.ContinuationCoordinator;
import jj.script.RhinoContext;
import jj.script.ScriptTask;
import jj.util.Closer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author jason
 *
 */
class ReplHandler extends SimpleChannelInboundHandler<String> {
	
	private final ResourceFinder resourceFinder;
	private final TaskRunner taskRunner;
	private final ContinuationCoordinator continuationCoordinator;
	private final CurrentReplChannelHandlerContext currentCtx;
	private final Provider<RhinoContext> contextProvider;
	
	@Inject
	ReplHandler(
		final ResourceFinder resourceFinder,
		final TaskRunner taskRunner,
		final ContinuationCoordinator continuationCoordinator,
		final CurrentReplChannelHandlerContext currentCtx,
		final Provider<RhinoContext> contextProvider
	) {
		this.resourceFinder = resourceFinder;
		this.taskRunner = taskRunner;
		this.continuationCoordinator = continuationCoordinator;
		this.currentCtx = currentCtx;
		this.contextProvider = contextProvider;
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush("Welcome to JibbrJabbr\n>");
	}

	@Override
	protected void channelRead0(final ChannelHandlerContext ctx, final String msg) throws Exception {
		
		final ReplScriptEnvironment rse = 
			resourceFinder.findResource(ReplScriptEnvironment.class, Virtual, ReplScriptEnvironment.BASE_REPL_SYSTEM);
		
		assert rse != null : "no ReplScriptEnvironment found!";
		
		try (RhinoContext context = contextProvider.get()) {
			
			final Script script = context.compileString(
				"$$print(function() { return " + msg + "; });",
				"repl"
			);

			taskRunner.execute(new ScriptTask<ReplScriptEnvironment>("repl execution:\n" + msg, rse, continuationCoordinator) {

				@Override
				protected void begin() throws Exception {
					
					try (Closer closer = currentCtx.enterScope(ctx)) {
						pendingKey = continuationCoordinator.execute(rse, script);
					}
				}
			});
			
		} catch (Exception e) {
			
			ctx.writeAndFlush(e.getMessage() + "\n>");
		}
	}

}
