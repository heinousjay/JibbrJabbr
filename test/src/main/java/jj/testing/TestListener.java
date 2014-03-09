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
package jj.testing;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.runner.Description;
import jj.JJServerStartupListener;
import jj.ServerStopping;
import jj.event.Listener;
import jj.event.Subscriber;

/**
 * @author jason
 *
 */
@Singleton
@Subscriber
public class TestListener implements JJServerStartupListener {
	
	private final TestLog testLog;
	private final Description description;
	
	@Inject
	TestListener(final TestLog testLog, final Description description) {
		this.testLog = testLog;
		this.description = description;
	}

	@Override
	public void start() throws Exception {
		testLog.info("{} - test start", description);
	}
	
	@Override
	public Priority startPriority() {
		return Priority.NearHighest;
	}

	@Listener
	public void stop(ServerStopping event) {
		testLog.info("{} - test end", description);
	}

}
