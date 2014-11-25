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

import static io.netty.handler.codec.http.HttpMethod.GET;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;

import jj.event.Publisher;
import jj.http.server.EngineHttpHandler;
import jj.http.server.HttpServerRequestImpl;
import jj.http.server.uri.Route;
import jj.http.server.uri.RouteMatch;
import jj.http.server.uri.Router;
import jj.http.server.uri.URIMatch;
import jj.http.server.websocket.WebSocketConnectionMaker;
import jj.http.server.websocket.WebSocketRequestChecker;
import jj.logging.Emergency;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.binder.AnnotatedBindingBuilder;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class EngineHttpHandlerTest {
	
	@Mock Publisher publisher;
	@Mock ChannelHandlerContext ctx;
	@Mock Channel channel;
	@Mock ChannelFuture channelFuture;
	@Mock Injector injector;
	@Mock Binder binder;
	@Mock AnnotatedBindingBuilder<Object> abb;
	@Captor ArgumentCaptor<Module> moduleCaptor;
	@Captor ArgumentCaptor<FullHttpResponse> responseCaptor;
	@Captor ArgumentCaptor<ChannelFutureListener> futureListenerCaptor;
	@Mock WebSocketRequestChecker webSocketRequestChecker;
	@Mock WebSocketConnectionMaker webSocketConnectionMaker;

	@Mock HttpServerRequestImpl httpRequest1;
	@Mock HttpServerRequestImpl httpRequest2;
	@Mock HttpServerRequestImpl httpRequest3;
	@Mock HttpServerRequestImpl httpRequest4;
	@Mock HttpServerRequestImpl httpRequest5;
	
	@Mock HttpServerResponse httpResponse;
	
	EngineHttpHandler handler;
	
	@Mock ServableResources servableResources;
	
	@Mock Router router;
	
	@Mock RouteMatch routeMatch;
	@Mock Route route;
	String resourceName = "resource";
	
	@Mock RouteProcessor routeProcessor;
	
	//given
	@Before
	public void before() throws Exception {
		
		handler = new EngineHttpHandler(servableResources, router, injector, webSocketRequestChecker, publisher);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void prepareInjectorStubbing() {
		// little ugly, but sets up an injector that will return our mocks
		given(injector.getInstance(HttpServerRequest.class)).willReturn(httpRequest1);
		given(injector.getInstance(HttpServerResponse.class)).willReturn(httpResponse);
		given(injector.getInstance(WebSocketConnectionMaker.class)).willReturn(webSocketConnectionMaker);
		given(injector.createChildInjector(any(Module.class))).willReturn(injector);
		given(injector.createChildInjector(any(Module.class), any(Module.class))).willReturn(injector);
		given(binder.bind((Class)any())).willReturn(abb);
	}
	
	@Test
	public void testReceivedBadRequest() throws Exception {
		FullHttpRequest fullHttpRequest = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
		given(fullHttpRequest.decoderResult().isSuccess()).willReturn(false);
		
		prepareInjectorStubbing();
		
		handler.messageReceived(ctx, fullHttpRequest);
		
		verify(httpResponse).sendError(HttpResponseStatus.BAD_REQUEST);
	}
	
	@Test
	public void testReceivedWebSocketRequest() throws Exception {
		
		FullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
		given(webSocketRequestChecker.isWebSocketRequest(fullHttpRequest)).willReturn(true);
		
		prepareInjectorStubbing();
		
		handler.messageReceived(ctx, fullHttpRequest);
		
		verify(webSocketConnectionMaker).handshakeWebsocket();
	}
	
	private void givenRouting() {
		given(routeMatch.route()).willReturn(route);
		given(routeMatch.resourceName()).willReturn(resourceName);
		given(router.routeRequest(any(HttpMethod.class), any(URIMatch.class))).willReturn(routeMatch);
	}
	
	@Test
	public void testReceivedHttpRequest() throws Exception {
		
		// given
		FullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, GET, "/");
		prepareInjectorStubbing();
		givenRouting();
		given(servableResources.routeProcessor(resourceName)).willReturn(routeProcessor);
		
		// when
		handler.messageReceived(ctx, fullHttpRequest);
		
		// then
		verify(injector).createChildInjector(moduleCaptor.capture());
		
		Module module = moduleCaptor.getValue();
		assertThat(module, is(notNullValue()));
		
		module.configure(binder);
		
		verify(binder).bind(ChannelHandlerContext.class);
		verify(abb).toInstance(ctx);
		verify(binder).bind(FullHttpRequest.class);
		verify(abb).toInstance(fullHttpRequest);
		verify(binder).bind(HttpServerRequest.class);
		verify(abb).to(HttpServerRequestImpl.class);
		verify(binder).bind(HttpServerResponse.class);
		verify(abb).to(HttpServerResponseImpl.class);
		
		verifyNoMoreInteractions(binder, abb);
		
		verify(routeProcessor).process(route, httpRequest1, httpResponse);
	}
	
	@Test
	public void testNotFound() throws Exception {
		// given
		FullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, GET, "/");
		prepareInjectorStubbing();
		givenRouting();
		
		// when
		handler.messageReceived(ctx, fullHttpRequest);
		
		// then
		verify(httpResponse).sendNotFound();
	}
	
	@Test
	public void testIOExceptionCaught() throws Exception {
		// IO exceptions are ignored at this point.  can't do anything about them anyway
		
		IOException ioe = new IOException();
		handler.exceptionCaught(ctx, ioe);
		verifyNoMoreInteractions(publisher);
		verifyNoMoreInteractions(ctx);
	}
	
	@Test
	public void testExceptionCaught() throws Exception {
		
		given(ctx.writeAndFlush(any())).willReturn(channelFuture);
		
		Throwable t = new Throwable();
		handler.exceptionCaught(ctx, t);
		
		// validate that the exception is logged because we
		// care about that
		verify(publisher).publish(isA(Emergency.class));
		verifyNoMoreInteractions(publisher);
		
		verify(ctx).writeAndFlush(responseCaptor.capture());
		
		FullHttpResponse response = responseCaptor.getValue();
		
		assertThat(response.status(), is(HttpResponseStatus.INTERNAL_SERVER_ERROR));
		
		verify(channelFuture).addListener(futureListenerCaptor.capture());
		
		assertThat(futureListenerCaptor.getValue(), is(ChannelFutureListener.CLOSE));
	}
	
	@Test
	public void testExceptionCaughtCausesException() throws Exception {
		
		//given
		RuntimeException second = new RuntimeException();
		given(ctx.writeAndFlush(any())).willThrow(second);
		
		//when
		Throwable t = new Throwable();
		handler.exceptionCaught(ctx, t);
		
		//then
		verify(publisher, times(2)).publish(isA(Emergency.class));
	}
}
