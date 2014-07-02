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
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import jj.document.DocumentScriptEnvironment;
import jj.http.server.websocket.WebSocketConnection;
import jj.http.server.websocket.WebSocketConnectionHost;
import jj.http.server.websocket.WebSocketFrameHandler;
import jj.http.server.websocket.WebSocketFrameHandlerCreator;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.binder.AnnotatedBindingBuilder;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketFrameHandlerCreatorTest {

	@Mock Injector injector;
	@InjectMocks WebSocketFrameHandlerCreator wsfhc;
	
	@Mock WebSocketServerHandshaker handshaker;
	@Mock DocumentScriptEnvironment scriptEnvironment;
	
	@Mock WebSocketFrameHandler handler;
	
	@Captor ArgumentCaptor<AbstractModule> moduleCaptor;
	
	@Mock Binder binder;
	@Mock AnnotatedBindingBuilder<Object> abb;
	
	@SuppressWarnings("unchecked")
	@Test
	public void test() {
		
		// given
		given(injector.createChildInjector(isA(AbstractModule.class))).willReturn(injector);
		given(injector.getInstance(WebSocketFrameHandler.class)).willReturn(handler);
		given(binder.bind(any(Class.class))).willReturn(abb);
		
		// when
		WebSocketFrameHandler result = wsfhc.createHandler(handshaker, scriptEnvironment);
		
		// then
		assertThat(result, is(handler));
		
		verify(injector).createChildInjector(moduleCaptor.capture());
		
		// when
		AbstractModule module = moduleCaptor.getValue();
		module.configure(binder);
		
		InOrder bindings = inOrder(binder,abb);
		
		// then
		bindings.verify(binder).bind(WebSocketConnection.class);
		bindings.verify(binder).bind(WebSocketFrameHandler.class);
		bindings.verify(binder).bind(WebSocketServerHandshaker.class);
		bindings.verify(abb).toInstance(handshaker);
		bindings.verify(binder).bind(WebSocketConnectionHost.class);
		bindings.verify(abb).toInstance(scriptEnvironment);
		
		verifyNoMoreInteractions(binder, abb);
		
	}

}
