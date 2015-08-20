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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import jj.ServerStarting.Priority;
import jj.event.MockPublisher;
import jj.event.MockPublisher.OnPublish;
import jj.execution.JJTask;
import jj.execution.MockTaskRunner;
import jj.server.Server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class JJServerLifecycleTest {
	
	@Mock Server server;
	MockPublisher publisher;
	MockTaskRunner taskRunner;
	
	
	@Mock Version version;
	
	@Mock JJTask<?> p1;
	@Mock JJTask<?> p2;
	@Mock JJTask<?> p3;
	
	@Mock JJTask<?> highest_task1;
	@Mock JJTask<?> highest_task2;
	@Mock JJTask<?> highest_task3;
	@Mock JJTask<?> highest_task4;
	
	@Mock JJTask<?> high_task1;
	@Mock JJTask<?> high_task2;
	@Mock JJTask<?> high_task3;
	@Mock JJTask<?> high_task4;
	
	@Mock JJTask<?> mid_task1;
	@Mock JJTask<?> mid_task2;
	@Mock JJTask<?> mid_task3;
	@Mock JJTask<?> mid_task4;
	
	@Mock JJTask<?> low_task1;
	@Mock JJTask<?> low_task2;
	@Mock JJTask<?> low_task3;
	@Mock JJTask<?> low_task4;
	
	@Mock JJTask<?> lowest_task1;
	@Mock JJTask<?> lowest_task2;
	@Mock JJTask<?> lowest_task3;
	@Mock JJTask<?> lowest_task4;
	
	
	List<Integer> order = new ArrayList<>();

	@Test
	public void test() throws Exception {
		
		// given
		publisher = new MockPublisher();
		publisher.onPublish = new OnPublish() {
			
			@Override
			public void published(Object event) {
				ServerStarting starting = (ServerStarting)event;
				starting.registerStartupTask(Priority.Highest, highest_task1);
				starting.registerStartupTask(Priority.Highest, highest_task2);
				starting.registerStartupTask(Priority.Highest, highest_task3);
				starting.registerStartupTask(Priority.Highest, highest_task4);
				starting.registerStartupTask(Priority.NearHighest, high_task1);
				starting.registerStartupTask(Priority.NearHighest, high_task2);
				starting.registerStartupTask(Priority.NearHighest, high_task3);
				starting.registerStartupTask(Priority.NearHighest, high_task4);
				starting.registerStartupTask(Priority.Middle, mid_task1);
				starting.registerStartupTask(Priority.Middle, mid_task2);
				starting.registerStartupTask(Priority.Middle, mid_task3);
				starting.registerStartupTask(Priority.Middle, mid_task4);
				starting.registerStartupTask(Priority.NearLowest, low_task1);
				starting.registerStartupTask(Priority.NearLowest, low_task2);
				starting.registerStartupTask(Priority.NearLowest, low_task3);
				starting.registerStartupTask(Priority.NearLowest, low_task4);
				starting.registerStartupTask(Priority.Lowest, lowest_task1);
				starting.registerStartupTask(Priority.Lowest, lowest_task2);
				starting.registerStartupTask(Priority.Lowest, lowest_task3);
				starting.registerStartupTask(Priority.Lowest, lowest_task4);
			}
		};
		taskRunner = new MockTaskRunner();
		
		JJServerLifecycle jsl = new JJServerLifecycle(server, publisher, taskRunner, version);
		
		// when
		jsl.start();
		
		// then
		
		assertTaskList();
		
		// given
		publisher.events.clear();
		publisher.onPublish = null;
		
		// when
		jsl.stop();
		
		// then
		assertThat(publisher.events.get(0), is(instanceOf(ServerStopping.class)));
	}

	@SuppressWarnings("unchecked")
	private void assertTaskList() {
		assertThat(taskRunner.tasks, hasItems(
			highest_task1, highest_task2, highest_task3, highest_task4,
			high_task1, high_task2, high_task3, high_task4,
			mid_task1, mid_task2, mid_task3, mid_task4,
			low_task1, low_task2, low_task3, low_task4,
			lowest_task1, lowest_task2, lowest_task3, lowest_task4
		));
	}

}
