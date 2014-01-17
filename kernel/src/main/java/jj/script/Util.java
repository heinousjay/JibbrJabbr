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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author jason
 *
 */
public class Util {
	
	private Util() {}
	
	public static String toJavaString(final Object fromScript) {
		try {
			return (String)Context.jsToJava(fromScript, String.class);
		} catch (EvaluatorException e) {
			return "";
		}
	}
	
	public static boolean toJavaBoolean(final Scriptable options, final String key) {
		try {
			return (boolean)Context.jsToJava(ScriptableObject.getProperty(options, key), Boolean.TYPE);
		} catch (Exception e) {
			return false;
		}
	}

}
