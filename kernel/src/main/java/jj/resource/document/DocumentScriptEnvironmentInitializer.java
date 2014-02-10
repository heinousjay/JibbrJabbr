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
package jj.resource.document;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.execution.JJExecutor;
import jj.execution.ScriptTask;
import jj.script.ContinuationCoordinator;
import jj.script.CurrentScriptContext;

/**
 * initializes a document script environment, so that it is ready for execution.  if possible :D
 * 
 * @author jason
 *
 */
@Singleton
class DocumentScriptEnvironmentInitializer {
	
	private final JJExecutor executor;
	
	private final ContinuationCoordinator continuationCoordinator;

	private final CurrentScriptContext context;
	
	/**
	 * @param name
	 * @param scriptEnvironment
	 */
	@Inject
	DocumentScriptEnvironmentInitializer(
		final JJExecutor executor,
		final ContinuationCoordinator continuationCoordinator,
		final CurrentScriptContext context
	) {
		this.executor = executor;
		this.continuationCoordinator = continuationCoordinator;
		this.context = context;
	}
	
	void initializeDocument(DocumentScriptEnvironment dse) {
		executor.execute(new InitializerTask("initializing document script environment at " + dse.baseName(), dse));
	}
	
	private class InitializerTask extends ScriptTask<DocumentScriptEnvironment> {

		/**
		 * @param name
		 * @param scriptEnvironment
		 */
		protected InitializerTask(String name, DocumentScriptEnvironment scriptEnvironment) {
			super(name, scriptEnvironment);
		}

		@Override
		protected void run() throws Exception {
			context.initialize(scriptEnvironment);
			try {
				continuationCoordinator.execute(scriptEnvironment);
			} finally {
				context.end();
			}
		}
		
		@Override
		protected void resume(String pendingKey) throws Exception {
			
		}
	
	}

}
