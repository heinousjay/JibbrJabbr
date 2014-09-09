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
package jj.http.server.uri;

import java.util.regex.Pattern;

class Parameter {
	
	enum Type {
		Param,
		Splat
	}
	
	final String name;
	final int start;
	final int end;
	final Type type;
	final Pattern pattern;
	
	Parameter(String name, int start, int end, final Type type, final Pattern pattern) {
		this.name = name;
		this.start = start;
		this.end = end;
		this.type = type;
		this.pattern = pattern;
	}
	
	@Override
	public String toString() {
		return type + " " + name + "@[" + start + "," + end + "]" + (pattern != null ? " with pattern " + pattern : "");
	}
}