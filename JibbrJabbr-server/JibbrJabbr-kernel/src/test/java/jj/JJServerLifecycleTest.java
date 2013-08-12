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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class JJServerLifecycleTest {
	
	List<Integer> order = new ArrayList<>();
	
	boolean stopped = false;
	
	class Start1 implements JJServerStartupListener {

		@Override
		public void start() throws Exception {
			order.add(1);
		}

		@Override
		public Priority startPriority() {
			return Priority.Highest;
		}
	}
	
	class Start2 implements JJServerStartupListener {

		@Override
		public void start() throws Exception {
			order.add(2);
		}

		@Override
		public Priority startPriority() {
			return Priority.NearHighest;
		}
	}
	
	class Start3 implements JJServerStartupListener {

		@Override
		public void start() throws Exception {
			order.add(3);
		}

		@Override
		public Priority startPriority() {
			return Priority.Middle;
		}
	}
	
	class Start4 implements JJServerStartupListener {

		@Override
		public void start() throws Exception {
			order.add(4);
		}

		@Override
		public Priority startPriority() {
			return Priority.NearLowest;
		}
	}
	
	class Start5 implements JJServerStartupListener, JJServerShutdownListener {

		@Override
		public void start() throws Exception {
			order.add(5);
		}

		@Override
		public Priority startPriority() {
			return Priority.Lowest;
		}
		
		@Override
		public void stop() {
			stopped = true;
		}
	}

	@Test
	public void test() throws Exception {
		
		// given
		
		Set<JJServerStartupListener> startups = new HashSet<>();
		startups.add(new Start3());
		startups.add(new Start2());
		startups.add(new Start5());
		startups.add(new Start4());
		startups.add(new Start1());
		
		Set<JJServerShutdownListener> shutdown = new HashSet<>();
		shutdown.add(new Start5());
		
		JJServerLifecycle jsl = new JJServerLifecycle(startups, shutdown);
		
		// when
		jsl.start();
		
		// then
		assertThat(order, contains(1, 2, 3, 4, 5));
		
		// when
		jsl.stop();
		
		// then
		assertThat(stopped, is(true));
	}

}
