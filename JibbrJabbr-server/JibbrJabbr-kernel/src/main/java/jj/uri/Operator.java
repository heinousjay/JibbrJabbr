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

import static jj.uri.UriTemplate.DEFAULT_SEPARATOR;

/**
 * @author jason
 *
 */
enum Operator {
	
	NUL         ("",  DEFAULT_SEPARATOR,  false, "",  Encoding.U), 
	RESERVED    ("+", DEFAULT_SEPARATOR,  false, "",  Encoding.UR), 
	NAME_LABEL  (".", ".",                false, "",  Encoding.U), 
	PATH        ("/", "/",                false, "",  Encoding.U), 
	MATRIX      (";", ";",                true,  "",  Encoding.U), 
	QUERY       ("?", "&",                true,  "=", Encoding.U), 
	CONTINUATION("&", "&",                true,  "=", Encoding.U),
	FRAGMENT    ("#", DEFAULT_SEPARATOR,  false, "",  Encoding.UF);
	
	private String operator;

	private String separator;

	private boolean named;

	private Encoding encoding = Encoding.U;

	private String empty = "";

	/**
	 * 
	 * Create a new Operator.
	 * 
	 * @param operator
	 * @param separator
	 */
	private Operator(
		String operator,
		String separator,
		boolean named,
		String empty,
		Encoding encoding
	) {
		this.operator = operator;
		this.separator = separator;
		this.named = named;
		this.encoding = encoding;
		this.empty = empty;
	}

	public String getOperator() {
		return this.operator;
	}

	public String getSeparator() {
		return this.separator;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public Encoding getEncoding() {
		return encoding;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public boolean isNamed() {
		return named;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public String ifEmptyString() {
		return empty;
	}

	/**
	    */
	public String getListSeparator() {
		return DEFAULT_SEPARATOR;
	}

	/**
	 * When the variable is a Collection, this flag determines if we use the
	 * VarSpec name to prefix values. For example:
	 * 
	 * {&list} return false
	 * 
	 * {&list*} will return true
	 * 
	 * @return
	 */
	public boolean useVarNameWhenExploded() {
		return named;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public String getPrefix() {
		return operator;
	}

	/**
	 * FIXME Comment this
	 * 
	 * @param opCode
	 * @return
	 */
	public static Operator fromOpCode(String opCode) {
		for (Operator op : Operator.values()) {
			if (op.getOperator().equalsIgnoreCase(opCode)) {
				return op;
			} else if (opCode.equalsIgnoreCase("!") || opCode.equalsIgnoreCase("=")) {
				throw new ExpressionParseException(opCode + " is not a valid operator.");
			}
		}
		return null;
	}
}
