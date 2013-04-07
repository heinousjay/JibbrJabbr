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
package jj;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.is;
import static jj.MockJJExecutors.ThreadType.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.jmock.lib.concurrent.DeterministicScheduler;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import jj.script.ScriptExecutorFactory;
import jj.script.ScriptRunner;

/**
 * Used to provide execution services for testing purposes.
 * 
 * @author jason
 *
 */
public class MockJJExecutors implements JJExecutors {
	
	public enum ThreadType {
		HttpControlThread,
		ScriptThread,
		IOThread;
	}
	
	private ScriptExecutorFactory scriptExecutorFactory = mock(ScriptExecutorFactory.class);
	private DeterministicScheduler executor;
	
	public MockJJExecutors(final DeterministicScheduler executor) {
		this.executor = executor;
		prepScriptExecutorFactory();
	}
	
	private void prepScriptExecutorFactory() {
		given(scriptExecutorFactory.executorFor(any(String.class))).will(new Answer<ScheduledExecutorService>() {

			@Override
			public ScheduledExecutorService answer(InvocationOnMock invocation) throws Throwable {
				executorSequence.add(ScriptThread);
				return executor;
			}
		});
	}

	public ScriptRunner scriptRunner = mock(ScriptRunner.class);
	
	@Override
	public ScriptRunner scriptRunner() {
		
		return scriptRunner;
	}
	
	private final List<ThreadType> executorSequence = new ArrayList<>();
	
	private int httpControlExecutorCount = 0;

	@Override
	public ScheduledExecutorService httpControlExecutor() {
		executorSequence.add(HttpControlThread);
		++httpControlExecutorCount;
		return executor;
	}
	
	public void assertHttpControlExecutorCountIs(int count) {
		assertThat("http control executor count in " +  executorSequence, httpControlExecutorCount, is(count));
	}
	
	private int ioExecutorCount = 0;

	@Override
	public ExecutorService ioExecutor() {
		executorSequence.add(IOThread);
		++ioExecutorCount;
		return executor;
	}
	
	public void assertIOExecutorCountIs(int count) {
		assertThat("io executor count in " +  executorSequence, ioExecutorCount, is(count));
	}

	@Override
	public ScriptExecutorFactory scriptExecutorFactory() {
		return scriptExecutorFactory;
	}

	@Override
	public ScheduledExecutorService scriptExecutorFor(String baseName) {
		executorSequence.add(ScriptThread);
		return executor;
	}
	
	public void assertExecutorSequence(ThreadType...threadTypes) {
		assertThat("jj executor sequence", threadTypes, is(executorSequence.toArray(new ThreadType[executorSequence.size()])));
	}
	
	private final Deque<ThreadType> threadTypeDeque = new ArrayDeque<>();
	
	/**
	 * 
	 * @param threadType
	 * @param number
	 */
	public void addThreadTypes(ThreadType threadType, int number) {
		for (int i = 0; i < number; ++i) {
			threadTypeDeque.addLast(threadType);
		}
	}
	
	public void assertThreadTypesEmpty() {
		assertTrue("thread types should be empty", threadTypeDeque.isEmpty());
	}
	
	public boolean isScriptThread = false;
	
	@Override
	public boolean isScriptThread() {
		return isScriptThread || (!threadTypeDeque.isEmpty() && threadTypeDeque.removeFirst() == ScriptThread);
	}
	
	public boolean isIOThread = false;

	@Override
	public boolean isIOThread() {
		return isIOThread || (!threadTypeDeque.isEmpty() && threadTypeDeque.removeFirst() == IOThread);
	}
	
	public boolean isHttpControlThread = false;

	@Override
	public boolean isHttpControlThread() {
		return isHttpControlThread || (!threadTypeDeque.isEmpty() && threadTypeDeque.removeFirst() == HttpControlThread);
	}
	
	public int ioPoolSize = 1;

	@Override
	public int ioPoolSize() {
		return ioPoolSize;
	}
	
	public final MockTaskCreator taskCreator = new MockTaskCreator();

	@Override
	public Runnable prepareTask(final JJRunnable task) {
		return taskCreator.prepareTask(task);
	}

}
