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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import jj.util.MockClock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TaskTrackerTest {

	MockClock clock = new MockClock();
	@Mock JJTask task;

	@Test
	public void test() {

		TaskTracker tt = new TaskTracker(clock, task);

		assertThat(tt.task(), is(task));

		assertThat(tt.startTime(), is(0L));
		assertThat(tt.enqueuedTime(), is(0L));
		assertThat(tt.executionTime(), is(0L));

		tt.enqueue();

		assertThat(tt.startTime(), is(0L));
		assertThat(tt.enqueuedTime(), is(-clock.time()));
		assertThat(tt.executionTime(), is(0L));

		clock.advance(1, SECONDS);

		tt.start();

		assertThat(tt.startTime(), is(clock.time()));
		assertThat(tt.enqueuedTime(), is(1000L));
		assertThat(tt.executionTime(), is(0L));

		clock.advance(3, MILLISECONDS);

		assertThat(tt.startTime(), is(clock.time() - 3));
		assertThat(tt.enqueuedTime(), is(1000L));
		assertThat(tt.executionTime(), is(0L));

		tt.end();

		assertThat(tt.startTime(), is(clock.time() - 3));
		assertThat(tt.enqueuedTime(), is(1000L));
		assertThat(tt.executionTime(), is(3L));
	}

}
