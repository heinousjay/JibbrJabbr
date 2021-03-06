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


/**
 * A component that can organize executions around the initialization of a script
 * environment.  
 * 
 * @author jason
 *
 */
public interface DependsOnScriptEnvironmentInitialization {

	/**
	 * register here to have a pendingKey resumed when a scriptEnvironment has transitioned to initialized
	 */
	void resumeOnInitialization(ScriptEnvironment<?> scriptEnvironment, PendingKey pendingKey);
	
	void executeOnInitialization(ScriptEnvironment<?> scriptEnvironment, ScriptTask<? extends ScriptEnvironment<?>> task);

}