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

import jj.script.ScriptEnvironment;
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
public class JJExecutorImplTest {
	
	@Mock ScriptRunner scriptRunner;
	@Mock IOExecutor ioExecutor;
	@Mock ScriptExecutorFactory scriptExecutorFactory;
	@Mock ScheduledExecutorService scriptExecutor;
	
	@Mock ScriptEnvironment scriptEnvironment;

	@InjectMocks ExecutorBundle bundle;
	
	@Mock CurrentTask currentTask;
	
	@Mock Logger logger;
	
	JJExecutorImpl executor;
	
	@Captor ArgumentCaptor<Runnable> runnableCaptor;
	
	String baseName = "test";
	
	@Before
	public void before() {
		
		executor = new JJExecutorImpl(bundle, currentTask, logger);
		
		given(scriptEnvironment.baseName()).willReturn(baseName);
		given(scriptExecutorFactory.executorFor(baseName)).willReturn(scriptExecutor);
	}
	
	private void runTask(ScheduledExecutorService service) {
		verify(service).submit(runnableCaptor.capture());
		runnableCaptor.getValue().run();
	}
	
	@Test
	public void testExecuteScriptTask() {
		
		// kind og a 
		
		final AtomicBoolean flag = new AtomicBoolean(false);
		
		ScriptTask<ScriptEnvironment> task = new ScriptTask<ScriptEnvironment>("test task", scriptEnvironment) {
			
			@Override
			protected void run() {
				flag.set(true);
			}
		};
		
		executor.execute(task);
		
		runTask(scriptExecutor);
		
		assertThat(flag.get(), is(true));
		verify(currentTask).set(task);
	}
	
	@Test
	public void testExecuteIOTask() {
		
		final AtomicBoolean flag = new AtomicBoolean(false);
		
		IOTask task = new IOTask("test task") {
			@Override
			protected void run() throws Exception {
				flag.set(true);
			}
		};
		
		executor.execute(task);
		
		runTask(ioExecutor);
		
		assertThat(flag.get(), is(true));
		verify(currentTask).set(task);
	}
	
	@Test
	public void testExecuteErrorLogged() {
		
		final Exception toThrow = new Exception();
		
		executor.execute(new ScriptTask<ScriptEnvironment>("test task", scriptEnvironment) {
			
			@Override
			protected void run() throws Exception {
				throw toThrow;
			}
		});
		
		executor.execute(new IOTask("test task") {
			
			@Override
			protected void run() throws Exception {
				throw toThrow;
			}
		});
		
		runTask(scriptExecutor);
		
		runTask(ioExecutor);
		
		verify(logger, times(2)).error(anyString(), eq(toThrow));
	}
}
