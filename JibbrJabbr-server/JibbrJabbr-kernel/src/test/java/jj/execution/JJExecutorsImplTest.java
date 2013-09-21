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
package jj.execution;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import jj.script.ScriptRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * @author jason
 *
 */


@RunWith(MockitoJUnitRunner.class)
public class JJExecutorsImplTest {
	
	@Mock ScriptRunner scriptRunner;
	@Mock IOExecutor ioExecutor;
	@Mock ScriptExecutorFactory scriptExecutorFactory;
	@Mock ScheduledExecutorService scriptExecutor;

	@InjectMocks ExecutorBundle bundle;
	
	@Mock Logger logger;
	
	JJExecutorsImpl executors;
	
	@Captor ArgumentCaptor<Runnable> runnableCaptor;
	
	@Before
	public void before() {
		
		executors = new JJExecutorsImpl(bundle, logger);
	}
	
	private void runTask(ScheduledExecutorService service) {
		verify(service).submit(runnableCaptor.capture());
		runnableCaptor.getValue().run();
	}
	
	@Test
	public void testExecuteScriptTask() {
		
		// kind og a 
		given(scriptExecutorFactory.executorFor("test")).willReturn(scriptExecutor);
		
		final AtomicBoolean flag = new AtomicBoolean(false);
		
		executors.execute(new ScriptTask("test task", "test") {
			
			@Override
			protected void run() {
				flag.set(true);
			}
		});
		
		runTask(scriptExecutor);
		
		assertThat(flag.get(), is(true));
	}
	
	@Test
	public void testExecuteIOTask() {
		
		final AtomicBoolean flag = new AtomicBoolean(false);
		
		executors.execute(new IOTask("test task") {
			@Override
			protected void run() throws Exception {
				flag.set(true);
			}
		});
		
		runTask(ioExecutor);
		
		assertThat(flag.get(), is(true));
	}
	
	@Test
	public void testExecuteErrorLogged() {
		
		given(scriptExecutorFactory.executorFor("test")).willReturn(scriptExecutor);
		
		final Exception toThrow = new Exception();
		
		executors.execute(new ScriptTask("test task", "test") {
			
			@Override
			protected void run() throws Exception {
				throw toThrow;
			}
		});
		
		executors.execute(new IOTask("test task") {
			
			@Override
			protected void run() throws Exception {
				throw toThrow;
			}
		});
		
		runTask(scriptExecutor);
		
		runTask(ioExecutor);
		
		verify(logger, times(2)).error(anyString(), eq(toThrow));
	}
	
	@Test
	public void testIgnoresOOM() {
		executors.execute(new IOTask("test task") {
			
			@Override
			protected void run() throws Exception {
				throw new OutOfMemoryError();
			}
		});
		
		try {
			runTask(ioExecutor);
			fail("should have thrown through here");
		} catch(OutOfMemoryError oom) {};
		
		verifyZeroInteractions(logger);
	}
}
