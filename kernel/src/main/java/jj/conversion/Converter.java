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

/**
 * Implement this to create a one-way conversion
 * @author jason
 *
 * @param <From> The incoming type
 * @param <To> The outgoing type
 */
public interface Converter<From, To> {

	/**
	 * <p>
	 * Convert the incoming value to the outgoing type. If the conversion cannot be
	 * performed, return null.
	 * 
	 * <p>
	 * DO NOT THROW EXCEPTIONS FROM THIS METHOD
	 * 
	 * @param in the incoming value
	 * @return the converted value
	 */
	To convert(From in);
}
