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
import jj.execution.MockJJExecutor;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.http.server.EngineHttpHandler;
import jj.http.server.JJHttpServerRequest;
import jj.http.server.WebSocketConnectionMaker;
import jj.http.server.servable.RequestProcessor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

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
	
	@Mock Logger logger;
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

	MockJJExecutor executors;

	@Mock JJHttpServerRequest httpRequest1;
	@Mock JJHttpServerRequest httpRequest2;
	@Mock JJHttpServerRequest httpRequest3;
	@Mock JJHttpServerRequest httpRequest4;
	@Mock JJHttpServerRequest httpRequest5;
	
	@Mock HttpResponse httpResponse;
	
	@Mock RequestProcessor requestProcessor1;
	@Mock RequestProcessor requestProcessor2;
	@Mock RequestProcessor requestProcessor3;
	
	EngineHttpHandler handler;
	
	@Rule
	public MockServablesRule servables = new MockServablesRule();
	
	//given
	@Before
	public void before() throws Exception {
		executors = new MockJJExecutor();
		
		given(httpRequest1.uriMatch()).willReturn(servables.staticUri);
		given(httpRequest2.uriMatch()).willReturn(servables.assetUri);
		given(httpRequest3.uriMatch()).willReturn(servables.cssUri);
		given(httpRequest4.uriMatch()).willReturn(servables.uri4);
		given(httpRequest5.uriMatch()).willReturn(servables.uri5);
		
		given(servables.staticServable.makeRequestProcessor(httpRequest1, httpResponse)).willReturn(requestProcessor1);
		given(servables.assetServable.makeRequestProcessor(httpRequest2, httpResponse)).willReturn(requestProcessor2);
		given(servables.cssServable.makeRequestProcessor(httpRequest3, httpResponse)).willReturn(requestProcessor3);
		given(servables.cssServable.makeRequestProcessor(httpRequest4, httpResponse)).willReturn(requestProcessor3);
		
		handler = new EngineHttpHandler(executors, servables.servables, injector, webSocketRequestChecker, logger);
	}

	@SuppressWarnings("unchecked")
	private void prepareInjectorStubbing() {
		// little ugly, but sets up an injector that will return our mocks
		given(injector.getInstance(HttpRequest.class)).willReturn(httpRequest1);
		given(injector.getInstance(HttpResponse.class)).willReturn(httpResponse);
		given(injector.getInstance(WebSocketConnectionMaker.class)).willReturn(webSocketConnectionMaker);
		given(injector.createChildInjector(any(Module.class))).willReturn(injector);
		given(binder.bind(any(Class.class))).willReturn(abb);
	}
	
	@Test
	public void testChannelRead0BadRequest() throws Exception {
		FullHttpRequest fullHttpRequest = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
		given(fullHttpRequest.getDecoderResult().isSuccess()).willReturn(false);
		
		prepareInjectorStubbing();
		
		handler.channelRead0(ctx, fullHttpRequest);
		
		verify(httpResponse).sendError(HttpResponseStatus.BAD_REQUEST);
	}
	
	@Test
	public void testChannelRead0WebSocketRequest() throws Exception {
		
		FullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
		given(webSocketRequestChecker.isWebSocketRequest(fullHttpRequest)).willReturn(true);
		
		prepareInjectorStubbing();
		
		handler.channelRead0(ctx, fullHttpRequest);
		
		verify(webSocketConnectionMaker).handshakeWebsocket();
	}
	
	@Test
	public void testChannelRead0HttpRequest() throws Exception {
		
		FullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
		
		prepareInjectorStubbing();
		
		handler.channelRead0(ctx, fullHttpRequest);
		
		verify(injector).createChildInjector(moduleCaptor.capture());
		
		Module module = moduleCaptor.getValue();
		assertThat(module, is(notNullValue()));
		
		module.configure(binder);
		
		verify(binder).bind(ChannelHandlerContext.class);
		verify(abb).toInstance(ctx);
		verify(binder).bind(FullHttpRequest.class);
		verify(abb).toInstance(fullHttpRequest);
		verify(binder).bind(HttpRequest.class);
		verify(abb).to(JJHttpServerRequest.class);
		verify(binder).bind(HttpResponse.class);
		verify(abb).to(JJHttpServerResponse.class);
		verify(binder).bind(WebSocketConnectionMaker.class);
		verify(binder).bind(WebSocketFrameHandlerCreator.class);
		
		verifyNoMoreInteractions(binder, abb);
	}
	
	@Test
	public void testIOExceptionCaught() throws Exception {
		// IO exceptions are ignored at this point.  can't do anything about them anyway
		
		IOException ioe = new IOException();
		handler.exceptionCaught(ctx, ioe);
		verifyNoMoreInteractions(logger);
		verifyNoMoreInteractions(ctx);
	}
	
	@Test
	public void testExceptionCaught() throws Exception {
		
		given(ctx.writeAndFlush(any())).willReturn(channelFuture);
		
		Throwable t = new Throwable();
		handler.exceptionCaught(ctx, t);
		
		// validate that the exception is logged because we
		// care about that
		verify(logger).error(anyString(), eq(t));
		verifyNoMoreInteractions(logger);
		
		verify(ctx).writeAndFlush(responseCaptor.capture());
		
		FullHttpResponse response = responseCaptor.getValue();
		
		assertThat(response.getStatus(), is(HttpResponseStatus.INTERNAL_SERVER_ERROR));
		
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
		verify(logger).error(anyString(), eq(t));
		verify(logger).error(anyString(), eq(second));
	}
	
	@Test
	public void testBasicOperation() throws Exception { 
		
		//when
		handler.handleHttpRequest(httpRequest1, httpResponse);
		executors.runUntilIdle();
		
		//then
		verify(requestProcessor1).process();
		
		//when
		handler.handleHttpRequest(httpRequest2, httpResponse);
		executors.runUntilIdle();
		
		//then
		verify(requestProcessor2).process();
		
		//when
		handler.handleHttpRequest(httpRequest3, httpResponse);
		executors.runUntilIdle();
		
		//then
		verify(requestProcessor3).process();
		
		//when
		handler.handleHttpRequest(httpRequest1, httpResponse);
		executors.runUntilIdle();
		
		//then
		verify(requestProcessor1, times(2)).process();
		
		//when
		handler.handleHttpRequest(httpRequest2, httpResponse);
		executors.runUntilIdle();
		
		//then
		verify(requestProcessor2, times(2)).process();
		
		//when
		handler.handleHttpRequest(httpRequest3, httpResponse);
		executors.runUntilIdle();
		
		//then
		verify(requestProcessor3, times(2)).process();
	}
	
	@Test
	public void testHandover() throws Exception {
		
		//when
		handler.handleHttpRequest(httpRequest4, httpResponse);
		executors.runUntilIdle();
		
		//then
		verify(requestProcessor3).process();
	}
	
	@Test
	public void testNotFound() throws Exception {
		
		//when
		handler.handleHttpRequest(httpRequest5, httpResponse);
		executors.runUntilIdle();
		
		// then
		verify(httpResponse).sendNotFound();
	}
	
	@Test
	public void testErrorDuringProcessing() throws Exception {
		
		//given
		IOException ioe = new IOException();
		doThrow(ioe).when(requestProcessor1).process();
		
		//when
		handler.handleHttpRequest(httpRequest1, httpResponse);
		executors.runUntilIdle();
		
		//then
		verify(httpResponse).error(ioe);
	}
}
