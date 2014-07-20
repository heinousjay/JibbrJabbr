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
package jj.jasmine;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockito.BDDMockito.*;

import jj.event.MockPublisher;
import jj.script.CurrentScriptEnvironment;
import jj.script.module.ScriptResource;
import jj.util.MockClock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class JasmineResultCollectorTest {

	MockClock clock = new MockClock();
	MockPublisher publisher = new MockPublisher();
	@Mock CurrentScriptEnvironment env;
	
	JasmineResultCollector jrc;
	
	@Mock JasmineScriptEnvironment jse;
	@Mock ScriptResource spec;
	@Mock ScriptResource target;
	
	@Mock Logger logger;
	
	@Before
	public void before() {
		given(jse.spec()).willReturn(spec);
		given(jse.target()).willReturn(target);
		
		given(env.currentAs(JasmineScriptEnvironment.class)).willReturn(jse);
		
		given(logger.isTraceEnabled()).willReturn(true);
		
		jrc = new JasmineResultCollector(clock, publisher, env);
	}
	
	@Test
	public void testSuccessRun() {
		// run through a success lifecycle and validate the results are logged
		// this doesn't do a lot of protection against anything internally yet,
		// so calling things out of order won't trip any assertions, it'll just
		// jack up the output
		
		jrc.jasmineStarted();
		
		// normal sort of run, including embedded suites, pending suites, and pending specs
		
		jrc.suiteStarted("suite1", "suite 1");
		jrc.specStarted("spec1", "spec 1");
		jrc.specDone("spec1", "passed");
		jrc.suiteStarted("suite2", "suite 2");
		jrc.specStarted("spec2", "spec 2");
		jrc.specDone("spec2", "passed");
		jrc.suiteDone("suite2", "suite 2");
		jrc.specStarted("spec3", "spec 3");
		jrc.specDone("spec3", "pending");
		jrc.suiteDone("suite1", "suite 1");
		jrc.suiteDone("suite3", "suite 3");
		
		clock.advance(20, MILLISECONDS);
		jrc.jasmineDone();
		
		JasmineTestSuccess jts = (JasmineTestSuccess)publisher.events.get(0);
		jts.describeTo(logger);
		
		verify(logger).info("Jasmine spec success!\nrunning {} succeeded\ntargeting {}\nexecution time: {}ms", spec, target, 20L);
		verify(logger).trace("results:\n{}", "\nsuite 1 - passed\n  spec 1 - passed\n  suite 2 - passed\n    spec 2 - passed\n  spec 3 - pending\n\nsuite 3 - pending\n");
	}
	
	@Test
	public void testFailureRun() {
		// run through a failure lifecycle and validate the results are logged

		
		jrc.jasmineStarted();
		
		// normal sort of run, including embedded suites, pending suites, and pending specs, with a single failed spec in the innermost spec
		
		jrc.suiteStarted("suite1", "suite 1");
		jrc.specStarted("spec1", "spec 1");
		jrc.specDone("spec1", "passed");
		jrc.suiteStarted("suite2", "suite 2");
		jrc.specStarted("spec2", "spec 2");
		jrc.specDone("spec2", "passed");
		jrc.specStarted("spec3", "spec 3");
		jrc.specExpectationFailed("spec3", "expected to pass");
		jrc.specDone("spec3", "failed");
		jrc.suiteDone("suite2", "suite 2");
		jrc.specStarted("spec4", "spec 4");
		jrc.specDone("spec4", "pending");
		jrc.suiteDone("suite1", "suite 1");
		jrc.suiteDone("suite3", "suite 3");
		
		clock.advance(20, MILLISECONDS);
		jrc.jasmineDone();
		
		JasmineTestFailure jts = (JasmineTestFailure)publisher.events.get(0);
		jts.describeTo(logger);

		String results = 
			"\nsuite 1 - failed\n  spec 1 - passed\n  suite 2 - failed\n    spec 2 - passed\n    spec 3 - failed\n     - expected to pass\n  spec 4 - pending\n\nsuite 3 - pending\n";
		
		verify(logger).error("Jasmine spec failure!\nrunning {} failed\ntargeting {}\nexecution time: {}ms\nresults:{}", spec, target, 20L, results);
	}

}
