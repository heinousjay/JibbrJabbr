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

import static jj.AnswerWithSelf.ANSWER_WITH_SELF;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Provider;

import jj.configuration.ConfigurationLoaded;
import jj.event.MockPublisher;
import jj.execution.MockTaskRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpServerTest {
	
	Provider<EngineHttpHandler> engineProvider = new Provider<EngineHttpHandler>() {
		
		@Override
		public EngineHttpHandler get() {
			return mock(EngineHttpHandler.class);
		}
	};
	
	@Mock HttpServerSwitch httpServerSwitch;
	
	ServerBootstrap serverBootstrap;
	
	Provider<ServerBootstrap> serverBootstrapProvider = new Provider<ServerBootstrap>() {
		@Override
		public ServerBootstrap get() {
			return serverBootstrap;
		}
	};
	
	MockPublisher publisher;
	MockTaskRunner taskRunner;
	MockHttpServerNioEventLoopGroup childGroup;
	
	@Mock HttpServerChannelInitializer initializer;
	@Mock UncaughtExceptionHandler uncaughtExceptionHandler;
	
	@Mock ChannelFuture future;
	
	@Mock EventLoopGroup bossGroup;
	
	@Mock Future<?> groupFuture;
	
	int timeout;
	boolean tcpNoDelay;
	int sendBufferSize;
	boolean reuseAddress;
	int receiveBufferSize;
	boolean keepAlive;
	int backlog;
	List<Binding> bindings;
	int hashCode;
	
	private void givenConfig1() {
		timeout = 10000;
		tcpNoDelay = true;
		sendBufferSize = 65536;
		reuseAddress = true;
		receiveBufferSize = 65536;
		keepAlive = true;
		backlog = 12;
		bindings =  Arrays.asList(new Binding(8080), new Binding("localhost", 8090));
		hashCode = 12;
	}
	
	HttpServerSocketConfiguration configuration = new HttpServerSocketConfiguration() {
		
		@Override
		public int timeout() {
			return timeout;
		}
		
		@Override
		public boolean tcpNoDelay() {
			return tcpNoDelay;
		}
		
		@Override
		public int sendBufferSize() {
			return sendBufferSize;
		}
		
		@Override
		public boolean reuseAddress() {
			return reuseAddress;
		}
		
		@Override
		public int receiveBufferSize() {
			return receiveBufferSize;
		}
		
		@Override
		public boolean keepAlive() {
			return keepAlive;
		}
		
		@Override
		public int backlog() {
			return backlog;
		}
		
		@Override
		public List<Binding> bindings() {
			return bindings;
		}
		
		public int hashCode() {
			return hashCode;
		}
	};
	
	HttpServer httpServer;
	
	@Before
	public void before() {
		serverBootstrap = mock(ServerBootstrap.class, ANSWER_WITH_SELF);
		publisher = new MockPublisher();
		taskRunner = new MockTaskRunner();
		childGroup = new MockHttpServerNioEventLoopGroup();
		httpServer = new HttpServer(
			childGroup,
			initializer,
			configuration,
			httpServerSwitch,
			publisher,
			taskRunner,
			serverBootstrapProvider,
			uncaughtExceptionHandler
		);
	}
	
	@After
	public void after() {
		timeout = -1;
		tcpNoDelay = false;
		sendBufferSize = -1;
		reuseAddress = false;
		receiveBufferSize = -1;
		keepAlive = false;
		backlog = -1;
		bindings =  null;
		hashCode = -1;
	}
	
	@Test
	public void testServerOff() throws Exception {
		
		// when
		httpServer.on((ConfigurationLoaded)null);
		
		// then
		assertTrue(publisher.events.isEmpty());
	}
	
	private void givenStartupConditions() {

		given(httpServerSwitch.on()).willReturn(true);
		given(serverBootstrap.bind(8080)).willReturn(future);
		given(serverBootstrap.bind("localhost", 8090)).willReturn(future);
	}
	
	@Captor ArgumentCaptor<EventLoopGroup> bossGroupCaptor;
	
	@Test
	public void testServerOnStartup() throws Exception {

		// given
		givenConfig1();
		givenStartupConditions();

		// when
		httpServer.on((ConfigurationLoaded)null);
		taskRunner.runFirstTask();
		
		// check more - the value of the bindings maybe?
		assertThat(publisher.events.get(0), is(instanceOf(BindingHttpServer.class)));
		assertThat(publisher.events.get(1), is(instanceOf(BindingHttpServer.class)));
		assertThat(publisher.events.get(2), is(instanceOf(HttpServerStarted.class)));
		
		verify(serverBootstrap).channel(NioServerSocketChannel.class);
		verify(serverBootstrap).group(bossGroupCaptor.capture(), eq(childGroup));
		verify(serverBootstrap).childHandler(initializer);
		verify(serverBootstrap).option(ChannelOption.SO_KEEPALIVE, configuration.keepAlive());
		verify(serverBootstrap).option(ChannelOption.SO_REUSEADDR, configuration.reuseAddress());
		verify(serverBootstrap).option(ChannelOption.TCP_NODELAY, configuration.tcpNoDelay());
		verify(serverBootstrap).option(ChannelOption.SO_TIMEOUT, configuration.timeout());
		verify(serverBootstrap).option(ChannelOption.SO_BACKLOG, configuration.backlog());
		verify(serverBootstrap).option(ChannelOption.SO_RCVBUF, configuration.receiveBufferSize());
		verify(serverBootstrap).option(ChannelOption.SO_SNDBUF, configuration.sendBufferSize());
		verify(future, times(2)).sync();
	}
	
	@Captor ArgumentCaptor<GenericFutureListener<Future<?>>> groupFutureListenerCaptor;
	
	@Test
	public void testServerRestart() throws Exception {

		// given
		givenConfig1();
		givenStartupConditions();

		// when
		httpServer.on((ConfigurationLoaded)null);
		taskRunner.runFirstTask();
		
		assertThat(publisher.events.size(), is(3));
		assertThat(publisher.events.get(0), is(instanceOf(BindingHttpServer.class)));
		assertThat(publisher.events.get(1), is(instanceOf(BindingHttpServer.class)));
		assertThat(publisher.events.get(2), is(instanceOf(HttpServerStarted.class)));
		publisher.events.clear();
		
		// given
		bindings = Arrays.asList(new Binding(8070));
		given(serverBootstrap.bind(8070)).willReturn(future);
		hashCode = 93987934; // just needs to be different
		given(serverBootstrap.group()).willReturn(bossGroup);
		willAnswer(new Answer<Future<?>>() {

			@Override
			public Future<?> answer(InvocationOnMock invocation) throws Throwable {
				@SuppressWarnings("unchecked")
				GenericFutureListener<Future<?>> listener = (GenericFutureListener<Future<?>>)invocation.getArguments()[0];
				listener.operationComplete(groupFuture);
				return groupFuture;
			}
			
		}).given(groupFuture).addListener(any());
		willReturn(groupFuture).given(bossGroup).shutdownGracefully(anyLong(), anyLong(), BDDMockito.any(TimeUnit.class));
		given(groupFuture.isSuccess()).willReturn(true);

		// when
		httpServer.on((ConfigurationLoaded)null);
		taskRunner.runFirstTask();
		
		// then
		assertThat(publisher.events.size(), is(3));
		assertThat(publisher.events.get(0), is(instanceOf(HttpServerRestarting.class)));
		assertThat(publisher.events.get(1), is(instanceOf(BindingHttpServer.class)));
		assertThat(publisher.events.get(2), is(instanceOf(HttpServerStarted.class)));
	}
}
