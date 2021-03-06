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

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.JJServerLifecycle;
import jj.configuration.ConfigurationLoaded;
import jj.event.Listener;
import jj.event.Subscriber;

import org.junit.runner.Description;
import org.slf4j.Logger;

@Singleton
@Subscriber
public class ServerLifecycleStatement extends JibbrJabbrTestStatement {
	
	private final JJServerLifecycle lifecycle;
	private final Logger testLog;
	private final Description description;
	private final Latch configured = new Latch(1);
	
	@Inject
	ServerLifecycleStatement(
		JJServerLifecycle lifecycle,
		@TestRunnerLogger Logger testLog,
		Description description
	) {
		this.lifecycle = lifecycle;
		this.testLog = testLog;
		this.description = description;
	}
	
	@Listener
	void on(ConfigurationLoaded event) {
		configured.countDown();
	}

	@Override
	public void evaluate() throws Throwable {
		long start = System.currentTimeMillis();
		try {
			testLog.info("============================================================");
			testLog.info("{} - test start", description);
			testLog.info(" Starting the server.");
			lifecycle.start();
			configured.await(500, TimeUnit.MILLISECONDS);
			evaluateInner();
		} finally {
			lifecycle.stop();
			testLog.info(" Server stopped.");
			testLog.info("{} - test end", description);
			testLog.info("finished in {} millis", System.currentTimeMillis() - start);
			testLog.info("============================================================");
		}
	}
	
}