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
import org.slf4j.Logger;

import jj.JJServerShutdownListener;
import jj.JJServerStartupListener;
import jj.logging.TestRunnerLogger;

/**
 * @author jason
 *
 */
@Singleton
public class TestListener implements JJServerStartupListener, JJServerShutdownListener {
	
	private final Logger testRunnerLogger;
	private final Description description;
	
	@Inject
	TestListener(final @TestRunnerLogger Logger testRunnerLogger, final Description description) {
		this.testRunnerLogger = testRunnerLogger;
		this.description = description;
	}

	@Override
	public void start() throws Exception {
		testRunnerLogger.info("{} - test start", description);
	}

	@Override
	public void stop() {
		testRunnerLogger.info("{} - test end", description);
	}

}
