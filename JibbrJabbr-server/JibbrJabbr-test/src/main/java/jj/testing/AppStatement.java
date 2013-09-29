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

import jj.JJServerLifecycle;
import jj.logging.TestRunnerLogger;

import org.junit.runners.model.Statement;
import org.slf4j.Logger;

@Singleton
public class AppStatement extends Statement {
	
	private final JJServerLifecycle lifecycle;
	private final Logger logger;
	private final Statement base;
	
	@Inject
	AppStatement(
		final JJServerLifecycle lifecycle,
		@TestRunnerLogger Logger logger,
		final Statement base
	) {
		this.lifecycle = lifecycle;
		this.logger = logger;
		this.base = base;
	}
	
	

	@Override
	public void evaluate() throws Throwable {
		long start = System.nanoTime();
		try {
			logger.info("============================================================");
			logger.info(" Starting the server.");
			lifecycle.start();
			base.evaluate();
		} finally {
			lifecycle.stop();
			logger.info(" Server stopped.");
			logger.info("finished in {} nanos", System.nanoTime() - start);
			logger.info("============================================================");
		}
	}
	
}