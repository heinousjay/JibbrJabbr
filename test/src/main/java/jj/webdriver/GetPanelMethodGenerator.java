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

import javassist.CtMethod;

/**
 * @author jason
 *
 */
class GetPanelMethodGenerator extends PanelMethodGenerator {

	@Override
	protected boolean matches(CtMethod newMethod, CtMethod baseMethod) throws Exception {
		
		return newMethod.getParameterTypes().length == 0 &&
			newMethod.getReturnType().getInterfaces().length == 1 &&
			newMethod.getReturnType().getInterfaces()[0].getName().equals(Panel.class.getName());
	}

	@Override
	protected void generateMethod(CtMethod newMethod, CtMethod baseMethod) throws Exception {
		StringBuilder sb = new StringBuilder("{")
			.append(newMethod.getReturnType().getName()).append(" result = makePanel(").append(newMethod.getReturnType().getName()). append(".class);");
		
		By by = (By)baseMethod.getAnnotation(By.class);
		if (by != null) {
			if (empty(by.value())) {
				throw new AssertionError("currently, By annotations on panel getter methods can only use the default value attribute.  this may change if needed!");
			}
			
			sb.append("((jj.webdriver.PanelBase)result).byStack(byStack.push(\"").append(by.value()).append("\"));");
		}
		
		sb.append("return result;}");
		
		setBody(newMethod, sb);
		
	}

}
