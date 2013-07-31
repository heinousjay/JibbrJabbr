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
import static jj.http.server.HttpServerChannelInitializer.PipelineStages.*;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import jj.script.AssociatedScriptBundle;
import jj.script.ScriptBundleFinder;
import jj.uri.URIMatch;

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
public class WebSocketConnectionMakerTest {
	
	@Mock WebSocketFrameHandlerCreator handlerCreator;
	@Mock ScriptBundleFinder scriptBundleFinder;
	@Mock AssociatedScriptBundle scriptBundle;
	@Mock Channel channel;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS) ChannelHandlerContext ctx;
	@Mock ChannelFuture channelFuture;
	@Captor ArgumentCaptor<ChannelFutureListener> futureListenerCaptor;
	@Mock FullHttpRequest request;
	@Mock WebSocketServerHandshakerFactory handshakerFactory;
	@Mock WebSocketServerHandshaker handshaker;
	@Mock WebSocketFrameHandler frameHandler;
	@Captor ArgumentCaptor<WebSocketFrameHandler> channelHandler;
	@Captor ArgumentCaptor<HttpResponse> responseCaptor;
	@Captor ArgumentCaptor<TextWebSocketFrame> textFrameCaptor;
	@Captor ArgumentCaptor<CloseWebSocketFrame> closeFrameCaptor;
	
	@InjectMocks WebSocketConnectionMaker wscm;

	@Test
	public void testValidConnection() throws Exception {
		
		// given
		given(ctx.channel()).willReturn(channel);
		given(handshakerFactory.newHandshaker(request)).willReturn(handshaker);
		given(handshaker.handshake(channel, request)).willReturn(channelFuture);
		
		// when
		wscm.handshakeWebsocket();
		
		// then
		verify(channelFuture).addListener(futureListenerCaptor.capture());
		
		// given
		String uri = "uri";
		given(request.getUri()).willReturn(uri);
		given(scriptBundleFinder.forURIMatch(any(URIMatch.class))).willReturn(scriptBundle);
		given(channelFuture.isSuccess()).willReturn(true);
		
		// when
		futureListenerCaptor.getValue().operationComplete(channelFuture);
		
		// then
		verify(handlerCreator).createHandler(handshaker, scriptBundle);
		verify(ctx.pipeline()).replace(eq(JJEngine.toString()), eq(JJWebsocketHandler.toString()), channelHandler.capture());
	}
	
	@Test
	public void testBadWebSocketRequest() throws Exception {
		
		// given
		given(handshakerFactory.newHandshaker(request)).willReturn(null);
		
		// when
		wscm.handshakeWebsocket();
		
		// then
		verify(ctx).writeAndFlush(responseCaptor.capture());
		verify(ctx.writeAndFlush(any())).addListener(ChannelFutureListener.CLOSE);
		
		HttpResponse response = responseCaptor.getValue();
		
		assertThat(response.getStatus(), is(HttpResponseStatus.UPGRADE_REQUIRED));
		assertThat(response.headers().get(HttpHeaders.Names.SEC_WEBSOCKET_VERSION), is(WebSocketVersion.V13.toHttpHeaderValue()));
	}
	
	@Test
	public void testObseleteScriptBundleConnection() throws Exception {
		
		// given
		given(ctx.channel()).willReturn(channel);
		given(handshakerFactory.newHandshaker(request)).willReturn(handshaker);
		given(handshaker.handshake(channel, request)).willReturn(channelFuture);
		
		// when
		wscm.handshakeWebsocket();
		
		// then
		verify(channelFuture).addListener(futureListenerCaptor.capture());
		
		// given
		String uri = "uri";
		given(request.getUri()).willReturn(uri);
		given(scriptBundleFinder.forURIMatch(any(URIMatch.class))).willReturn(null);
		given(channelFuture.isSuccess()).willReturn(true);
		
		// when
		futureListenerCaptor.getValue().operationComplete(channelFuture);
		
		// then
		verify(ctx).writeAndFlush(textFrameCaptor.capture());
		
		assertThat(textFrameCaptor.getValue().text(), is("jj-reload"));
		verify(ctx.writeAndFlush(textFrameCaptor.getValue())).addListener(futureListenerCaptor.capture());
		
		// resetting ctx here to eliminate the earlier verifications we've already done
		// so our close frame capture works
		reset(ctx);
		
		// when
		futureListenerCaptor.getValue().operationComplete(channelFuture);
		
		// then
		verify(ctx).writeAndFlush(closeFrameCaptor.capture());
		assertThat(closeFrameCaptor.getValue().statusCode(), is(1000));
		verify(ctx.writeAndFlush(closeFrameCaptor.getValue())).addListener(ChannelFutureListener.CLOSE);
	}
	
	@Test
	public void testFailedConnection() throws Exception {
		
		// given
		given(ctx.channel()).willReturn(channel);
		given(handshakerFactory.newHandshaker(request)).willReturn(handshaker);
		given(handshaker.handshake(channel, request)).willReturn(channelFuture);
		
		// when
		wscm.handshakeWebsocket();
		
		// then
		verify(channelFuture).addListener(futureListenerCaptor.capture());
		
		// given
		given(channelFuture.isSuccess()).willReturn(false);
		
		// when 
		futureListenerCaptor.getValue().operationComplete(channelFuture);
		
		// then
		verify(ctx).close();
	}

}
