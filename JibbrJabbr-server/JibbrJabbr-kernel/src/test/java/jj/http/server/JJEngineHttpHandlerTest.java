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

import static org.mockito.BDDMockito.*;

import java.util.LinkedHashSet;
import java.util.Set;

import jj.execution.ExecutionTrace;
import jj.execution.MockJJExecutors;
import jj.http.HttpResponse;
import jj.http.server.JJEngineHttpHandler;
import jj.http.server.JJHttpServerRequest;
import jj.http.server.WebSocketConnectionMaker;
import jj.http.server.servable.RequestProcessor;
import jj.http.server.servable.Servable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.google.inject.Injector;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class JJEngineHttpHandlerTest {
	
	@Mock Logger logger;
	@Mock ExecutionTrace trace;
	@Mock Injector injector;
	@Mock WebSocketConnectionMaker webSocketConnectionMaker;

	MockJJExecutors executors;
	@Mock Servable servable1;
	@Mock Servable servable2;
	@Mock Servable servable3;

	@Mock JJHttpServerRequest httpRequest1;
	@Mock JJHttpServerRequest httpRequest2;
	@Mock JJHttpServerRequest httpRequest3;
	@Mock JJHttpServerRequest httpRequest4;
	
	@Mock HttpResponse httpResponse;
	
	@Mock RequestProcessor requestProcessor1;
	@Mock RequestProcessor requestProcessor2;
	@Mock RequestProcessor requestProcessor3;
	Set<Servable> resourceTypes;
	
	//given
	@Before
	public void before() throws Exception {
		executors = new MockJJExecutors();
		
		given(servable1.isMatchingRequest(httpRequest1)).willReturn(true);
		given(servable1.isMatchingRequest(httpRequest2)).willReturn(false);
		given(servable1.isMatchingRequest(httpRequest3)).willReturn(false);
		given(servable1.isMatchingRequest(httpRequest4)).willReturn(false);
		given(servable1.makeRequestProcessor(httpRequest1, httpResponse)).willReturn(requestProcessor1);
		
		given(servable2.isMatchingRequest(httpRequest1)).willReturn(false);
		given(servable2.isMatchingRequest(httpRequest2)).willReturn(true);
		given(servable2.isMatchingRequest(httpRequest3)).willReturn(false);
		given(servable2.isMatchingRequest(httpRequest4)).willReturn(true);
		given(servable2.makeRequestProcessor(httpRequest2, httpResponse)).willReturn(requestProcessor2);
		given(servable2.makeRequestProcessor(httpRequest4, httpResponse)).willReturn(null);
		
		given(servable3.isMatchingRequest(httpRequest1)).willReturn(false);
		given(servable3.isMatchingRequest(httpRequest2)).willReturn(false);
		given(servable3.isMatchingRequest(httpRequest3)).willReturn(true);
		given(servable3.isMatchingRequest(httpRequest4)).willReturn(true);
		given(servable3.makeRequestProcessor(httpRequest3, httpResponse)).willReturn(requestProcessor3);
		given(servable3.makeRequestProcessor(httpRequest4, httpResponse)).willReturn(requestProcessor3);
		
		resourceTypes = new LinkedHashSet<>();
		resourceTypes.add(servable1);
		resourceTypes.add(servable2);
		resourceTypes.add(servable3);
	}
	
	@Test
	public void testBasicOperation() throws Exception {
		
		JJEngineHttpHandler handler = new JJEngineHttpHandler(executors, resourceTypes, injector, trace, webSocketConnectionMaker);
		
		//when
		handler.handleHttpRequest(httpRequest1, httpResponse);
		executors.executor.runUntilIdle();
		
		//then
		verify(servable1).isMatchingRequest(httpRequest1);
		verify(requestProcessor1).process();
		
		//when
		handler.handleHttpRequest(httpRequest2, httpResponse);
		executors.executor.runUntilIdle();
		
		//then
		verify(servable2).isMatchingRequest(httpRequest2);
		verify(requestProcessor2).process();
		
		//when
		handler.handleHttpRequest(httpRequest3, httpResponse);
		executors.executor.runUntilIdle();
		
		//then
		verify(servable3).isMatchingRequest(httpRequest3);
		verify(requestProcessor3).process();
		
		//when
		handler.handleHttpRequest(httpRequest1, httpResponse);
		executors.executor.runUntilIdle();
		
		//then
		verify(requestProcessor1, times(2)).process();
		
		//when
		handler.handleHttpRequest(httpRequest2, httpResponse);
		executors.executor.runUntilIdle();
		
		//then
		verify(requestProcessor2, times(2)).process();
		
		//when
		handler.handleHttpRequest(httpRequest3, httpResponse);
		executors.executor.runUntilIdle();
		
		//then
		verify(requestProcessor3, times(2)).process();
	}
	
	@Test
	public void testHandover() throws Exception {
		
		JJEngineHttpHandler handler = new JJEngineHttpHandler(executors, resourceTypes, injector, trace, webSocketConnectionMaker);
		
		//when
		handler.handleHttpRequest(httpRequest4, httpResponse);
		executors.executor.runUntilIdle();
		
		//then
		verify(servable2).isMatchingRequest(httpRequest4);
		verify(servable3).isMatchingRequest(httpRequest4);
		
		verify(requestProcessor3).process();
	}
}
