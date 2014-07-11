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

import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.inject.Inject;

import jj.event.Publisher;

/**
 * collects results from a jasmine run, collates them into
 * something sensible, and publishes them
 *
 * @author jason
 *
 */
public class JasmineResultCollector {

	private final Publisher publisher;
	
	private static class Suite extends Spec {
		
		Suite(String id, String description, Suite parent) {
			super(id, description, parent);
		}
		
		void finish() {
			if (!children.isEmpty()) {
				status = "passed";
				// and look for failures and change to failure instead
				for (Spec child : children.values()) {
					if (child.status == "failed") {
						status = "failed";
						break;
					}
				}
			}
		}
		
		@Override
		public String toString() {
			return makeToString(new StringBuilder(), 0).toString();
		}
	}
	
	private static class Spec {
		
		final String description;
		final Suite parent;
		final LinkedHashMap<String, Spec> children = new LinkedHashMap<>();
		final ArrayList<String> failedExpectations = new ArrayList<>();
		
		String status = "pending";
		
		Spec(String id, String description, Suite parent) {
			this.description = description;
			this.parent = parent;
			if (parent != null) {
				parent.children.put(id, this);
			}
		}
		
		protected StringBuilder makeToString(StringBuilder sb, int indentation) {
			
			for (int i = 0; i < indentation; ++i) {
				sb.append(' ');
			}
			
			sb.append(description).append(" - ").append(status).append('\n');
			
			for (Spec child : children.values()) {
				child.makeToString(sb, indentation + 2);
			}
			
			return sb;
		}
	}
	
	private final LinkedHashMap<String, Suite> suites = new LinkedHashMap<>();
	
	private Suite currentSuite = null;
	
	@Inject
	JasmineResultCollector(final Publisher publisher) {
		this.publisher = publisher;
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
		// status is "null" in specs that called pending();
		currentSuite.children.get(id).status = status;
	}
	
	public void jasmineDone() {
		for (Suite suite : suites.values()) {
			System.out.println(suite);
		}
		publisher.publish(this);
	}
}
