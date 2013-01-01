/*
 * adapted from https://github.com/damnhandy/Handy-URI-Templates
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
package jj.uri;

public class ExpressionParseException extends RuntimeException {

	/** The serialVersionUID */
	private static final long serialVersionUID = -3912835265423887511L;

	/**
	 * Create a new InvalidExpressionException.
	 * 
	 */
	ExpressionParseException() {
		super();
	}

	/**
	 * Create a new InvalidExpressionException.
	 * 
	 * @param message
	 * @param cause
	 */
	ExpressionParseException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new InvalidExpressionException.
	 * 
	 * @param message
	 */
	ExpressionParseException(String message) {
		super(message);
	}

	/**
	 * Create a new InvalidExpressionException.
	 * 
	 * @param cause
	 */
	ExpressionParseException(Throwable cause) {
		super(cause);
	}

}