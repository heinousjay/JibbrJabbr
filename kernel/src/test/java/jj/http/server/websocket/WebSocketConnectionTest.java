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
package jj.http.server.websocket;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.net.InetSocketAddress;

import jj.document.DocumentScriptEnvironment;
import jj.http.server.websocket.WebSocketConnection;
import jj.jjmessage.JJMessage;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Callable;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketConnectionTest {
	
	@Mock(answer = Answers.RETURNS_DEEP_STUBS) ChannelHandlerContext ctx;
	@Mock DocumentScriptEnvironment documentScriptEnvironment;
	@Captor ArgumentCaptor<TextWebSocketFrame> textFrameCaptor;
	@Captor ArgumentCaptor<CloseWebSocketFrame> closeFrameCaptor;
	WebSocketConnection connection;
	
	@Mock Callable function1;
	@Mock Callable function2;
	
	@Before
	public void before() {
		
		given(ctx.channel().remoteAddress()).willReturn(InetSocketAddress.createUnresolved("localhost", 8080));
		
		connection = new WebSocketConnection(ctx, documentScriptEnvironment);
	}

	@Test
	public void testSend() {
		JJMessage message1 = JJMessage.makeBind("context", "selector", "bind");
		JJMessage message2 = JJMessage.makeInvoke("invoke", "[0]");
		String sent = "[" + message1 + "," + message2 + "]";
		connection.send(message1).send(message2).exitedScope();
		
		verify(ctx).writeAndFlush(textFrameCaptor.capture());
		
		assertThat(textFrameCaptor.getValue().text(), is(sent));
	}
	
	@Test
	public void testFunctionStorage() {
		String name1 = "name1";
		String name2 = "name2";
		connection.addFunction(name1, function1);
		
		assertThat(connection.getFunction(name1), is(function1));
		assertThat(connection.getFunction(name2), is(nullValue()));
		assertFalse(connection.removeFunction(name2));
		assertTrue(connection.removeFunction(name1));
		assertFalse(connection.removeFunction(name1));
		
		connection.addFunction(name2, function2);

		assertThat(connection.getFunction(name1), is(nullValue()));
		assertThat(connection.getFunction(name2), is(function2));
		assertFalse(connection.removeFunction(name1));
		assertFalse(connection.removeFunction(name2, function1));
		assertTrue(connection.removeFunction(name2, function2));
		assertFalse(connection.removeFunction(name2));
	}
	
	@Test
	public void testClose() {
		
		connection.close();
		
		verify(ctx).writeAndFlush(closeFrameCaptor.capture());
		
		assertThat(closeFrameCaptor.getValue().statusCode(), is(1000));
		
		verify(ctx.writeAndFlush(any())).addListener(ChannelFutureListener.CLOSE);
	}
	
	@Test
	public void testActivityMarker() {
		
		long now1 = System.currentTimeMillis();
		connection.markActivity();
		long now2 = System.currentTimeMillis();
		
		assertTrue(now1 <= connection.lastActivity());
		assertTrue(connection.lastActivity() <= now2);
	}

}
