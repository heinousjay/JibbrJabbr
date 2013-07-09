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
package jj.execution;

import java.lang.Thread.UncaughtExceptionHandler;

import jj.JJModule;

/**
 * @author jason
 *
 */
public class ExecutionModule extends JJModule {

	@Override
	protected void configure() {
		
		addServerListenerBinding().to(IOExecutor.class);
		addServerListenerBinding().to(ScriptExecutorFactory.class);
		addServerListenerBinding().to(ClientExecutor.class);

		
		// for now.  this will have different installations in different
		// environments, i'd think.  if you replace this binding you'll also
		// have to  do something with TaskCreator
		bind(ExecutionTrace.class).to(ExecutionTraceImpl.class);
		
		// a good place to break apart crafty circular dependencies.  this is
		// the most popular object in the system.  for good reason.
		bind(JJExecutors.class).to(JJExecutorsImpl.class);
		
		bind(UncaughtExceptionHandler.class).to(JJUncaughtExceptionHandler.class);
		
	}

}
