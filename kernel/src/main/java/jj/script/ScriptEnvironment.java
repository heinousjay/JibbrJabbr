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

import jj.resource.Resource;

import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * <p>
 * an execution environment for continuable rhino scripts.  specific implementations define
 * the semantics of that environment.
 * 
 * <p>
 * do not implement this directly, extend {@link AbstractScriptEnvironment} instead
 * 
 * @author jason
 *
 */
public interface ScriptEnvironment extends Resource {

	/**
	 * The execution scope for the script
	 */
	Scriptable scope();

	/**
	 * The compiled script instance. Could be null if the environment is purely virtual,
	 * such as a module based on a deserialized JSON object
	 */
	Script script();
	
	/**
	 * The name of the script
	 */
	String scriptName();
	
	/**
	 * true when this environment is fully initialized, false prior
	 */
	boolean initialized();
	
	/**
	 * true when this environment has begun initializing but before initialization is complete.
	 * false prior
	 */
	boolean initializing();

	/**
	 * the error that occurred during initialization, if any
	 */
	Throwable initializationError();
	
	/**
	 * true if there is an initialization error.
	 */
	boolean initializationDidError();

	/**
	 * Get a new, empty script object in the scope of this environment
	 */
	ScriptableObject newObject();

	/**
	 * The exports of this environment. may be null, may be literally anything the script decides to export
	 */
	Object exports();

}