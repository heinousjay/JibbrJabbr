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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import io.netty.channel.ChannelHandlerContext;
import jj.execution.MockTaskRunner;
import jj.resource.ResourceFinder;
import jj.script.ContinuationCoordinator;
import jj.script.MockRhinoContextProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Script;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ReplHandlerTest {
	
	@Mock ResourceFinder resourceFinder;
	MockTaskRunner taskRunner;
	@Mock ContinuationCoordinator continuationCoordinator;
	MockRhinoContextProvider contextProvider;
	
	ReplHandler rh;
	
	@Mock ChannelHandlerContext ctx;
	
	@Mock ReplScriptEnvironment rse;
	
	@Mock Script script;

	@Before
	public void before() throws Exception {
		taskRunner = new MockTaskRunner();
		contextProvider = new MockRhinoContextProvider();
		
		rh = new ReplHandler(
			resourceFinder,
			taskRunner,
			continuationCoordinator,
			new CurrentReplChannelHandlerContext(),
			contextProvider
		);
		
		given(rse.alive()).willReturn(true);
		given(resourceFinder.findResource(ReplScriptEnvironment.class, Virtual, ReplScriptEnvironment.NAME)).willReturn(rse);
	}
	
	@Test
	public void testWelcome() throws Exception {
		rh.handlerAdded(ctx);
		
		verify(ctx).writeAndFlush("Welcome to JibbrJabbr\n>");
	}
	
	@Test
	public void testEmptyMessage() throws Exception {
		
		rh.messageReceived(ctx, "   \n\r\n\r\n   ");

		verify(ctx).writeAndFlush(">");
		
		assertThat(taskRunner.tasks, is(empty()));
	}
	
	@Test
	public void testMessageSuccess() throws Exception {
		
		given(contextProvider.context.compileString("$$print(function() { return something; });", "repl-console")).willReturn(script);
		rh.messageReceived(ctx, "something");
		
		taskRunner.runFirstTask();
		
		verify(continuationCoordinator).execute(rse, script);
	}
	
	@Test
	public void testMessageError() throws Exception {
		
		RuntimeException e = new RuntimeException("this is an exception");

		given(contextProvider.context.compileString("$$print(function() { return something; });", "repl-console")).willThrow(e);
		rh.messageReceived(ctx, "something");
		
		verify(ctx).writeAndFlush(e.getMessage() + "\n>");
		
		assertThat(taskRunner.tasks, is(empty()));
	}

}
