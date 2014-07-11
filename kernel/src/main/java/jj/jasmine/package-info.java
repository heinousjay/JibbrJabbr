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
/**
 * <p>
 * wraps up the jasmine spec runner so it can be used for testing. currently,
 * the integration stays out of the way - it merely listens for some events,
 * and when those events happen, it does some stuff.
 * 
 * <p>
 * The lifecycle is like so:
 * <ul>
 *   <li>listen for a ScriptResource to get loaded
 *   <li>attempt to create a corresponding JasmineScriptEnvironment
 *   <li>look for a spec script named the same as the original script, with -spec in the name
 *   <li>if found, run it
 *   <li>publish the results
 * </ul>
 * 
 * @author jason
 *
 */
package jj.jasmine;