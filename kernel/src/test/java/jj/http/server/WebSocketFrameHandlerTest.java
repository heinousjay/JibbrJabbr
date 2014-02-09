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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketFrameHandlerTest {
	
	@Mock WebSocketServerHandshaker handshaker;
	@Mock WebSocketHandler handler;
	@Mock WebSocketConnection connection;
	@Mock WebSocketConnectionHost webSocketConnectionHost;
	@Mock WebSocketConnectionTracker connectionTracker;
	@InjectMocks WebSocketFrameHandler wsfh;
	
	// don't mock things you don't own!
	// unless, as in this case, the things you don't
	// own won't work without talking to an actual resource
	@Mock(answer=Answers.RETURNS_DEEP_STUBS) ChannelHandlerContext ctx;
	
	@Captor ArgumentCaptor<ChannelFutureListener> futureListenerCaptor;
	ChannelFuture future;

	ByteBuf byteBuf;
	@Captor ArgumentCaptor<TextWebSocketFrame> textFrameCaptor;
	@Captor ArgumentCaptor<PongWebSocketFrame> pongFrameCaptor;
	
	@Before
	public void before() {
		byteBuf = Unpooled.buffer(0);
		
		given(connection.webSocketConnectionHost()).willReturn(webSocketConnectionHost);
	}
	
	@Test
	public void testHandlerAdded() throws Exception {
		
		wsfh.handlerAdded(ctx);
		
		verify(connectionTracker).addConnection(connection);
		verify(webSocketConnectionHost).connected(connection);
		verify(handler).opened(connection);
		verify(ctx.channel().closeFuture()).addListener(futureListenerCaptor.capture());
		
		ChannelFutureListener listener = futureListenerCaptor.getValue();
		
		listener.operationComplete(future);
		
		verify(connectionTracker).removeConnection(connection);
		verify(webSocketConnectionHost).disconnected(connection);
		verify(handler).closed(connection);
	}
	
	@Test
	public void testTextFrame() throws Exception {
		
		String text = "text";
		TextWebSocketFrame textFrame = new TextWebSocketFrame(text);
		
		wsfh.channelRead0(ctx, textFrame);
		
		verify(connection).markActivity();
		verify(webSocketConnectionHost).message(connection, text);
	}
	
	@Test
	public void testHeartbeatTextFrame() throws Exception {
		
		TextWebSocketFrame textFrame = new TextWebSocketFrame("jj-hi");
		
		wsfh.channelRead0(ctx, textFrame);
		
		verify(connection).markActivity();
		verifyZeroInteractions(handler);
		verify(ctx).writeAndFlush(textFrameCaptor.capture());
		assertThat(textFrameCaptor.getValue().text(), is("jj-yo"));
	}
	
	@Test
	public void testPingFrame() throws Exception {
		
		PingWebSocketFrame pingFrame = new PingWebSocketFrame(byteBuf);
		
		wsfh.channelRead0(ctx, pingFrame);
		
		verify(connection).markActivity();
		verify(ctx).writeAndFlush(pongFrameCaptor.capture());
		PongWebSocketFrame pongFrame2 = pongFrameCaptor.getValue().retain();
		assertThat(pongFrame2.content(), is(byteBuf));
	}
	
	@Test
	public void testPongFrame() throws Exception {
		
		PongWebSocketFrame pongFrame = new PongWebSocketFrame(byteBuf);
		
		wsfh.channelRead0(ctx, pongFrame);
		
		verify(connection).markActivity();
		verify(handler).ponged(connection, byteBuf);
	}
	
	@Test
	public void testBinaryFrame() throws Exception {
		
		BinaryWebSocketFrame binaryFrame = new BinaryWebSocketFrame(byteBuf);
		
		wsfh.channelRead0(ctx, binaryFrame);
		
		verify(connection).markActivity();
		verify(handler).messageReceived(connection, byteBuf);
	}
	
	@Test
	public void testCloseFrame() throws Exception {
		
		CloseWebSocketFrame closeFrame = new CloseWebSocketFrame(1000, "");
		
		wsfh.channelRead0(ctx, closeFrame);
		
		verify(handshaker).close(ctx.channel(), closeFrame);
	}

}
