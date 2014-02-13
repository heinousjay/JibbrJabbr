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
package jj.script;

import jj.event.Publisher;
import jj.execution.TaskRunner;

/**
 * just raises the visibility of some methods for testing
 * 
 * @author jason
 *
 */
public class MockableScriptEnvironmentInitializer extends ScriptEnvironmentInitializer {

	/**
	 * @param taskRunner
	 * @param continuationCoordinator
	 */
	public MockableScriptEnvironmentInitializer(
		TaskRunner taskRunner,
		IsScriptThread isScriptThread,
		ContinuationCoordinatorImpl continuationCoordinator,
		Publisher publisher
	) {
		super(taskRunner, isScriptThread, continuationCoordinator, publisher);
	}

	
	@Override
	public void initializeScript(AbstractScriptEnvironment se) {
		super.initializeScript(se);
	}
	
	public void scriptEnvironmentInitialized(ScriptEnvironment scriptEnvironment) {
		
	}
}
