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
import static jj.http.server.PipelineStages.*;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import jj.AnswerWithSelf;
import jj.http.HttpResponse;
import jj.resource.ResourceFinder;
import jj.resource.document.DocumentScriptEnvironment;
import jj.uri.URIMatch;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketConnectionMakerTest {
	
	@Mock WebSocketFrameHandlerCreator handlerCreator;

	@Mock DocumentScriptEnvironment scriptEnvironment;
	@Mock ResourceFinder resourceFinder;
	
	@Mock Channel channel;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS) ChannelHandlerContext ctx;
	@Mock ChannelFuture channelFuture;
	@Captor ArgumentCaptor<ChannelFutureListener> futureListenerCaptor;
	@Mock FullHttpRequest request;
	HttpResponse response;
	@Mock WebSocketServerHandshakerFactory handshakerFactory;
	@Mock WebSocketServerHandshaker handshaker;
	@Mock WebSocketFrameHandler frameHandler;
	@Captor ArgumentCaptor<WebSocketFrameHandler> channelHandler;

	@Captor ArgumentCaptor<TextWebSocketFrame> textFrameCaptor;
	@Captor ArgumentCaptor<CloseWebSocketFrame> closeFrameCaptor;
	
	WebSocketConnectionMaker wscm;

	@Before
	public void before() {
		
		response = mock(HttpResponse.class, AnswerWithSelf.ANSWER_WITH_SELF);
		
		wscm = new WebSocketConnectionMaker(handlerCreator, resourceFinder, ctx, request, response, handshakerFactory);
	}
	
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
		String sha = "1234567890123456789012345678901234567890";
		String uri = "/" + sha + "/somethign.socket";
		given(scriptEnvironment.sha1()).willReturn(sha);
		given(request.getUri()).willReturn(uri);
		given(resourceFinder.findResource(DocumentScriptEnvironment.class, new URIMatch(uri).name)).willReturn(scriptEnvironment);
		given(channelFuture.isSuccess()).willReturn(true);
		
		// when
		futureListenerCaptor.getValue().operationComplete(channelFuture);
		
		// then
		verify(handlerCreator).createHandler(handshaker, scriptEnvironment);
		verify(ctx.pipeline()).replace(eq(JJEngine.toString()), eq(JJWebsocketHandler.toString()), channelHandler.capture());
	}
	
	@Test
	public void testBadWebSocketRequest() throws Exception {
		
		// given
		given(handshakerFactory.newHandshaker(request)).willReturn(null);
		
		// when
		wscm.handshakeWebsocket();
		
		// then
		verify(response).header(HttpHeaders.Names.SEC_WEBSOCKET_VERSION, WebSocketVersion.V13.toHttpHeaderValue());
		verify(response).sendError(HttpResponseStatus.UPGRADE_REQUIRED);
	}
	
	@Test
	public void testObseleteScriptExecutionEnvironmentConnection() throws Exception {
		
		// given
		given(ctx.channel()).willReturn(channel);
		given(handshakerFactory.newHandshaker(request)).willReturn(handshaker);
		given(handshaker.handshake(channel, request)).willReturn(channelFuture);
		
		// when
		wscm.handshakeWebsocket();
		
		// then
		verify(channelFuture).addListener(futureListenerCaptor.capture());
		
		// given
		String uri = "/1234567890123456789012345678901234567890/uri.socket";
		given(request.getUri()).willReturn(uri);
		given(scriptEnvironment.sha1()).willReturn("ABCDEF");
		given(resourceFinder.findResource(eq(DocumentScriptEnvironment.class), anyString())).willReturn(scriptEnvironment);
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
		assertThat(closeFrameCaptor.getValue().statusCode(), is(1001));
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