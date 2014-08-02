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
package jj.conversion;

import java.util.HashSet;
import java.util.Set;

/**
 * @author jason
 *
 */
public class ConverterSetMaker {

	public static Set<Converter<?, ?>> converters() {
		HashSet<Converter<?, ?>> output = new HashSet<>();
		output.add(new FromStringToBoolean());
		output.add(new FromStringToPath());
		output.add(new FromStringToInteger());
		output.add(new FromStringToLong());
		output.add(new FromDoubleToInteger());
		return output;
	}
}
