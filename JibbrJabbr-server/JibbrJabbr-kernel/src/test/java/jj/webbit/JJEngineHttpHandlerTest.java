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
package jj.webbit;

import static org.mockito.BDDMockito.*;

import java.util.LinkedHashSet;
import java.util.Set;

import jj.MockJJExecutors;
import jj.servable.Servable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class JJEngineHttpHandlerTest {

	MockJJExecutors executors;
	@Mock Servable servable1;
	@Mock Servable servable2;
	@Mock Servable servable3;
	@Mock JJHttpRequestCreator creator;
	@Mock HttpRequest request1;
	@Mock HttpRequest request2;
	@Mock HttpRequest request3;
	@Mock HttpRequest request4;
	@Mock JJHttpRequest httpRequest1;
	@Mock JJHttpRequest httpRequest2;
	@Mock JJHttpRequest httpRequest3;
	@Mock JJHttpRequest httpRequest4;
	@Mock HttpResponse response;
	@Mock HttpControl control;
	@Mock RequestProcessor requestProcessor1;
	@Mock RequestProcessor requestProcessor2;
	@Mock RequestProcessor requestProcessor3;
	Set<Servable> resourceTypes;
	
	//given
	@Before
	public void before() throws Exception {
		executors = new MockJJExecutors();
		
		given(creator.createJJHttpRequest(request1)).willReturn(httpRequest1);
		given(creator.createJJHttpRequest(request2)).willReturn(httpRequest2);
		given(creator.createJJHttpRequest(request3)).willReturn(httpRequest3);
		given(creator.createJJHttpRequest(request4)).willReturn(httpRequest4);
		
		given(servable1.isMatchingRequest(httpRequest1)).willReturn(true);
		given(servable1.isMatchingRequest(httpRequest2)).willReturn(false);
		given(servable1.isMatchingRequest(httpRequest3)).willReturn(false);
		given(servable1.isMatchingRequest(httpRequest4)).willReturn(false);
		given(servable1.makeRequestProcessor(httpRequest1, response, control)).willReturn(requestProcessor1);
		
		given(servable2.isMatchingRequest(httpRequest1)).willReturn(false);
		given(servable2.isMatchingRequest(httpRequest2)).willReturn(true);
		given(servable2.isMatchingRequest(httpRequest3)).willReturn(false);
		given(servable2.isMatchingRequest(httpRequest4)).willReturn(true);
		given(servable2.makeRequestProcessor(httpRequest2, response, control)).willReturn(requestProcessor2);
		given(servable2.makeRequestProcessor(httpRequest4, response, control)).willReturn(null);
		
		given(servable3.isMatchingRequest(httpRequest1)).willReturn(false);
		given(servable3.isMatchingRequest(httpRequest2)).willReturn(false);
		given(servable3.isMatchingRequest(httpRequest3)).willReturn(true);
		given(servable3.isMatchingRequest(httpRequest4)).willReturn(true);
		given(servable3.makeRequestProcessor(httpRequest3, response, control)).willReturn(requestProcessor3);
		given(servable3.makeRequestProcessor(httpRequest4, response, control)).willReturn(requestProcessor3);
		
		resourceTypes = new LinkedHashSet<>();
		resourceTypes.add(servable1);
		resourceTypes.add(servable2);
		resourceTypes.add(servable3);
	}
	
	@Test
	public void testBasicOperation() throws Exception {
		
		JJEngineHttpHandler handler = new JJEngineHttpHandler(executors, creator, resourceTypes);
		
		//when
		handler.handleHttpRequest(request1, response, control);
		executors.executor.runUntilIdle();
		
		//then
		verify(servable1).isMatchingRequest(httpRequest1);
		verify(requestProcessor1).process();
		
		//when
		handler.handleHttpRequest(request2, response, control);
		executors.executor.runUntilIdle();
		
		//then
		verify(servable2).isMatchingRequest(httpRequest2);
		verify(requestProcessor2).process();
		
		//when
		handler.handleHttpRequest(request3, response, control);
		executors.executor.runUntilIdle();
		
		//then
		verify(servable3).isMatchingRequest(httpRequest3);
		verify(requestProcessor3).process();
		
		//when
		handler.handleHttpRequest(request1, response, control);
		executors.executor.runUntilIdle();
		
		//then
		verify(requestProcessor1, times(2)).process();
		
		//when
		handler.handleHttpRequest(request2, response, control);
		executors.executor.runUntilIdle();
		
		//then
		verify(requestProcessor2, times(2)).process();
		
		//when
		handler.handleHttpRequest(request3, response, control);
		executors.executor.runUntilIdle();
		
		//then
		verify(requestProcessor3, times(2)).process();
		
		verify(control, never()).nextHandler();
	}
	
	@Test
	public void testHandover() throws Exception {
		
		JJEngineHttpHandler handler = new JJEngineHttpHandler(executors, creator, resourceTypes);
		
		//when
		handler.handleHttpRequest(request4, response, control);
		executors.executor.runUntilIdle();
		
		//then
		verify(servable2).isMatchingRequest(httpRequest4);
		verify(servable3).isMatchingRequest(httpRequest4);
		
		verify(requestProcessor3).process();
		verify(control, never()).nextHandler();
	}
}
