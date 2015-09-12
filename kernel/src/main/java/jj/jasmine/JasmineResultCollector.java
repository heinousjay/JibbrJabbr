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

import java.time.Clock;
import java.util.LinkedHashMap;

import javax.inject.Inject;

import jj.event.Publisher;
import jj.script.CurrentScriptEnvironment;

/**
 * collects results from a jasmine run, collates them into
 * something sensible, and publishes them
 *
 * @author jason
 *
 */
public class JasmineResultCollector {
	
	private final Clock clock;

	private final Publisher publisher;
	
	private final JasmineScriptEnvironment jse;
	
	private final LinkedHashMap<String, Suite> suites = new LinkedHashMap<>();
	
	private long startTime;
	
	private Suite currentSuite = null;
	
	@Inject
	JasmineResultCollector(
		final Clock clock,
		final Publisher publisher,
		final CurrentScriptEnvironment env
	) {
		this.clock = clock;
		this.publisher = publisher;
		jse = env.currentAs(JasmineScriptEnvironment.class);
	}
	
	public void suiteStarted(String id, String description) {
		suites.put(id, new Suite(id, description, currentSuite));
		currentSuite = suites.get(id);
	}
	
	public void suiteDone(String id, String description) {
		Suite suite = suites.get(id);
		if (suite == null) {
			// this was xdescribe and did not execute, just keep it as a note
			// we won't know anything about the specs, it'll just be choked out
			suite = new Suite(id, description, currentSuite);
			suites.put(id, suite);
		}
		
		suite.finish();
		currentSuite = suite.parent;
		
		if (currentSuite != null) {
			suites.remove(id); // only keep the top-level suites in this list upon completion
		}
	}
	
	public void specStarted(String id, String description) {
		assert currentSuite != null;
		currentSuite.children.put(id, new Spec(id, description, currentSuite));
	}
	
	public void specExpectationFailed(String id, String expectationMessage) {
		assert currentSuite != null;
		assert currentSuite.children.get(id) != null;
		currentSuite.children.get(id).failedExpectations.add(expectationMessage);
	}
	
	public void specDone(String id, String status) {
		assert currentSuite != null;
		assert currentSuite.children.get(id) != null;
		currentSuite.children.get(id).status = status;
	}
	
	public void jasmineStarted() {
		startTime = clock.millis();
	}
	
	public void jasmineDone() {
		// if there are any failures, publish
		// JasmineSpecFailure
		// else
		// JasmineSpecSuccess
		// with the stringified suite information
		boolean failed = false;
		for (Suite suite : suites.values()) {
			failed = failed || suite.failed();
		}
		long executionTime = clock.millis() - startTime;
		publisher.publish(
			failed ? 
			new JasmineTestFailure(jse, executionTime, suites.values()) :
			new JasmineTestSuccess(jse, executionTime, suites.values())
		);
	}
}
