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

/**
 * @author jason
 * 
 */
public class VariableExpansionException extends RuntimeException {

	/** The serialVersionUID */
	private static final long serialVersionUID = -1927979719672747848L;

	/**
	 * Create a new VariableExpansionException.
	 * 
	 */
	VariableExpansionException() {

	}

	/**
	 * Create a new VariableExpansionException.
	 * 
	 * @param message
	 */
	VariableExpansionException(String message) {
		super(message);
	}

	/**
	 * Create a new VariableExpansionException.
	 * 
	 * @param cause
	 */
	VariableExpansionException(Throwable cause) {
		super(cause);
	}

	/**
	 * Create a new VariableExpansionException.
	 * 
	 * @param message
	 * @param cause
	 */
	VariableExpansionException(String message, Throwable cause) {
		super(message, cause);
	}

}
