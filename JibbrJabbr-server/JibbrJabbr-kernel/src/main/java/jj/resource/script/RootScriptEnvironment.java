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
package jj.resource.script;

import jj.script.ScriptEnvironment;

/**
 * denotes a script environment as being a container for other script environments,
 * in particular the API and document varieties can contain modules and specs.
 * 
 * @author jason
 *
 */
public interface RootScriptEnvironment extends ScriptEnvironment {

}
