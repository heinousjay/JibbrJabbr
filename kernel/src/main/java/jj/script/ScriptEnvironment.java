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
 * @author jason
 *
 */
public interface ScriptEnvironment extends Resource {

	/**
	 * The execution scope for the script
	 * @return
	 */
	Scriptable scope();

	/**
	 * The script
	 * @return
	 */
	Script script();
	
	/**
	 * (hopefully) unique ID for the script, based on the resources that
	 * compose the execution environment
	 * @return
	 */
	String sha1();
	
	/**
	 * 
	 * @return
	 */
	String scriptName();

	boolean initialized();
	
	void initialized(boolean initialized);
	
	boolean initializing();
	
	void initializing(boolean initializing);
	
	String baseName();

	/**
	 * Get a new, empty script object in the scope of this environment
	 */
	ScriptableObject newObject();

}