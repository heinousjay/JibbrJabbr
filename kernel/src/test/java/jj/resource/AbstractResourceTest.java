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
package jj.resource;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.isA;

import jj.event.Publisher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 * validates that dependencies are handled correctly
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractResourceTest {

	@Mock Publisher publisher;
	
	@Test
	public void testDependencyAndLifetimeInteraction() {
		
		final AbstractResource base = new MyResource("", publisher);
		final AbstractResource one = new MyResource("1", publisher);
		final AbstractResource two = new MyResource("2", publisher);
		final AbstractResource one_two = new MyResource("1/2", publisher);
		
		base.addDependent(one);
		base.addDependent(two);
		one.addDependent(one_two);
		
		// validate the starting state
		assertThat(base.dependents(), containsInAnyOrder(one, two));
		assertThat(one.dependents(), contains(one_two));
		assertThat(two.dependents(), is(empty()));
		assertThat(one_two.dependents(), is(empty()));
		
		assertTrue(base.alive());
		assertTrue(one.alive());
		assertTrue(two.alive());
		assertTrue(one_two.alive());
		
		// okay this is a little weird, i admit it
		
		willAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				ResourceKilled event = (ResourceKilled)invocation.getArguments()[0];
				base.on(event);
				one.on(event);
				two.on(event);
				one_two.on(event);
				return null;
			}
		}).given(publisher).publish(isA(ResourceKilled.class));
		
		one_two.kill();
		two.kill();

		assertThat(base.dependents(), containsInAnyOrder(one));
		assertThat(one.dependents(), is(empty()));
		assertThat(two.dependents(), is(empty()));
		assertThat(one_two.dependents(), is(empty()));
		
		assertTrue(base.alive());
		assertTrue(one.alive());
		assertFalse(two.alive());
		assertFalse(one_two.alive());
	}
}
