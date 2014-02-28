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
package jj.webdriver;

import java.util.regex.Pattern;

import javassist.CtMethod;

/**
 * <p>
 * Generates an implementation for a method matching a pattern defined as
 * 
 * <ul>
 * <li>Annotated with {@link By}
 * <li>A method name starting with "click" followed by a capital letter, a number, an underscore, or $
 * <li>Zero parameters
 * <li>The standard return
 * </ul>
 * 
 * @author jason
 *
 */
// test coverage by jj.webdriver.PageFactoryTest
class ClickMethodGenerator extends PanelMethodGenerator {
	
	private static final Pattern NAME = makeNamePattern("click");

	@Override
	protected boolean matches(CtMethod newMethod, CtMethod baseMethod) throws Exception {
		return hasBy(baseMethod) &&
			NAME.matcher(newMethod.getName()).find() &&
			newMethod.getParameterTypes().length == 0 &&
			isStandardReturn(newMethod);
	}
	
	@Override
	protected void generate(CtMethod newMethod, CtMethod baseMethod) throws Exception {
		StringBuilder sb = new StringBuilder("{");
		sb.append("click(").append(makeByFromMethod(baseMethod)).append(");");
		generateStandardReturn(newMethod, sb);
		sb.append("}");
		
		newMethod.setBody(sb.toString());
	}
}
