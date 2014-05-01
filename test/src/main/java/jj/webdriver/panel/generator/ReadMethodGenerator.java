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
package jj.webdriver.panel.generator;

import java.util.regex.Pattern;

import javassist.CtMethod;
import jj.webdriver.By;
import jj.webdriver.panel.PanelMethodGenerator;

/**
 * @author jason
 *
 */
public class ReadMethodGenerator extends PanelMethodGenerator {
	
	private static final Pattern NAME = makeNamePattern("read");
	
	@Override
	protected boolean matches(CtMethod newMethod, CtMethod baseMethod) throws Exception {
		return hasBy(baseMethod) &&
			NAME.matcher(newMethod.getName()).find() &&
			parametersMatchByAnnotation(0, newMethod, baseMethod) &&
			newMethod.getReturnType().getName().equals("java.lang.String");
	}

	@Override
	protected void generateMethod(CtMethod newMethod, CtMethod baseMethod) throws Exception {
		StringBuilder sb = new StringBuilder("{");
		processBy((By)baseMethod.getAnnotation(By.class), LOCAL_BY, 0, sb);
		sb.append("return read(").append(LOCAL_BY).append(");");
		sb.append("}");
		setBody(newMethod, sb);
	}
	
}
