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
package jj.script;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import jj.execution.MockJJExecutors;
import jj.hostapi.ScriptJSON;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class RestRequestContinuationProcessorTest {

	@Mock ScriptContext scriptContext;
	@Mock CurrentScriptContext context;
	@Mock AsyncHttpClient client;
	@Mock ListenableFuture<Response> future;
	MockJJExecutors executors;
	@Mock ScriptJSON json;
	@Mock ContinuationState continuationState;
	@Mock Runnable returnedRunnable;
	Runnable runnable;
	@Mock Response response;
	
	@Before
	public void before() throws Exception {
		executors = new MockJJExecutors();
		
		RestRequest restRequest = new RestRequest(null);
		given(continuationState.restRequest()).willReturn(restRequest);
		
		given(client.executeRequest(null)).willReturn(future);
		
		given(future.addListener(BDDMockito.any(Runnable.class), eq(executors.executor))).will(new Answer<ListenableFuture<Response> >() {

			@Override
			public ListenableFuture<Response>  answer(InvocationOnMock invocation) throws Throwable {
				runnable = (Runnable)invocation.getArguments()[0];
				return future;
			}
		});
		
		given(context.save()).willReturn(scriptContext);
	}
	
	@Test
	public void test() throws Exception {
		
		//given
		
		//when
		new RestRequestContinuationProcessor(context, client, executors, json).process(continuationState);
		
		//then
		assertThat(runnable, is(notNullValue()));
		verify(future).addListener(BDDMockito.any(Runnable.class), eq(executors.executor));
		
		//given
		given(future.get()).willReturn(response);
		
		//when
		runnable.run();
		
		//then
		verify(context).restore(scriptContext);
		verify(executors.scriptRunner).restartAfterContinuation(BDDMockito.any(String.class), BDDMockito.any(Object.class));
		verify(json).parse(null);
	}

}
