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
package jj.http.server;

import jj.conversion.Converter;

/**
 * @author jason
 *
 */
public class FromObjectArrayToBinding implements Converter<Object[], Binding> {

	@Override
	public Binding convert(final Object[] in) {
		if (in.length == 2 && (in[0] instanceof String) && (in[1] instanceof Number)) {
			return new Binding((String)in[0], ((Number)in[1]).intValue());
		} else if (in.length == 1 && (in[0] instanceof Number)) {
			return new Binding(((Number)in[0]).intValue());
		}
		return null;
	}
}
