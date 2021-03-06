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

import org.mozilla.javascript.Callable;

/**
 * An object that holds script functions according to some lifetime,
 * organized by name, for example to be registered as event listeners
 * 
 * @author jason
 *
 */
public interface FunctionContext {

	Callable getFunction(String name);

	void addFunction(String name, Callable function);

	boolean removeFunction(String name);

	boolean removeFunction(String name, Callable function);

}