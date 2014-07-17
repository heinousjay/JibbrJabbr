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
package org.mozilla.javascript;

/**
 * The cheatingest test technique in the world.
 * 
 * none of this is a particularly great practice but it's fun!
 * @author jason
 *
 */
public class RhinoContextTestHelper {
	
	public static boolean isInterpretedScript(final Script script) {
		return script instanceof InterpretedFunction;
	}

	public static int getLanguageVersion(final Script script) {
		if (isInterpretedScript(script)) {
			return ((InterpretedFunction)script).getLanguageVersion();
		}
		
		throw new AssertionError();
	}
}
