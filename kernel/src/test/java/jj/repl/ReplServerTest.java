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

import static java.util.concurrent.TimeUnit.SECONDS;
import static jj.configuration.resolution.AppLocation.Virtual;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.util.concurrent.CountDownLatch;

import javax.inject.Provider;

import jj.event.Publisher;
import jj.resource.ResourceLoader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ReplServerTest {
	
	ReplConfiguration configuration = new ReplConfiguration() {
		
		@Override
		public int port() {
			return -1;
		}
		
		@Override
		public boolean activate() {
			return true;
		}
	};
	
	Provider<ReplHandler> replHandlerProvider = new Provider<ReplHandler>() {

		@Override
		public ReplHandler get() {
			return mock(ReplHandler.class);
		}
	};
	
	@Mock Publisher publisher;
	@Mock ResourceLoader resourceLoader;
	
	ReplServer server;
	
	@Before
	public void before() {
		server = new ReplServer(configuration, new ReplServerChannelInitializer(replHandlerProvider), publisher, resourceLoader);
	}
	
	@After
	public void after() {
		server.serverStopping(null);
	}
	
	@Test
	public void testInitialization() {
		server.serverStarting(null);
		
		verify(resourceLoader).loadResource(ReplScriptEnvironment.class, Virtual, ReplScriptEnvironment.NAME);
	}

	@Test
	public void testConnecting() throws Exception {
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		willAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				latch.countDown();
				return null;
			}
		}).given(publisher).publish(isA(ReplListening.class));
		
		server.configurationLoaded(null);
		
		assertTrue(latch.await(1, SECONDS)); 
		
		// if we got here, it worked
	}

}
