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
package jj.jqmessage;

/**
 * @author jason
 *
 */
public class MessageMaker {

	public static JQueryMessage makeEvent(final String selector, final String type) {
		JQueryMessage result = new JQueryMessage();
		result.event(new Event());
		result.event().selector = selector;
		result.event().type = type;
		return result;
	}
	
	public static JQueryMessage makeResult(final String id, final String value) {
		JQueryMessage result = new JQueryMessage();
		result.result(new Result());
		result.result().id = id;
		result.result().value = value;
		return result;
	}
}
