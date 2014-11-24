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
package jj;

import org.mockito.internal.stubbing.defaultanswers.ReturnsEmptyValues;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * A simple answer to configure a mock with a fluent interface to avoid
 * stubbing every method in use.
 * 
 * @author jason
 * 
 */
public class AnswerWithSelf implements Answer<Object> {

	public static final Answer<Object> ANSWER_WITH_SELF = new AnswerWithSelf();
	
	private final Answer<Object> delegate = new ReturnsEmptyValues();

	public Object answer(InvocationOnMock invocation) throws Throwable {
		Class<?> returnType = invocation.getMethod().getReturnType();
		if (returnType.isAssignableFrom(invocation.getMock().getClass())) {
			return invocation.getMock();
		} else {
			return delegate.answer(invocation);
		}
	}
}
