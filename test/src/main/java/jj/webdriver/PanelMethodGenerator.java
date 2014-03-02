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

import javassist.CtClass;
import javassist.CtMethod;

/**
 * @author jason
 *
 */
public abstract class PanelMethodGenerator {
	
	/**
	 * 
	 */
	protected static final String LOCAL_BY = "localBy";

	protected static final Pattern makeNamePattern(String name) {
		return Pattern.compile("^" + name + "[A-Z\\d_\\$]");
	}

	protected abstract boolean matches(CtMethod newMethod, CtMethod baseMethod) throws Exception;
	
	protected boolean hasBy(CtMethod baseMethod) {
		return baseMethod.hasAnnotation(By.class);
	}
	
	private boolean hasPanelInterface(CtClass type) throws Exception {
		String name = type.getInterfaces()[0].getName();
		return Panel.class.getName().equals(name) || Page.class.getName().equals(name);
	}
	
	protected boolean isStandardReturn(CtMethod newMethod) throws Exception {
		
		CtClass returnType = newMethod.getReturnType();
		return returnType.getName().equals("void") || hasPanelInterface(returnType);
	}
	
	private boolean isPanel(CtClass type) throws Exception {
		return type.getInterfaces().length == 1 && Panel.class.getName().equals(type.getInterfaces()[0].getName());
	}
	
	private boolean isPage(CtClass type) throws Exception {
		return type.getInterfaces().length == 1 && Page.class.getName().equals(type.getInterfaces()[0].getName());
	}
	
	protected void generate(CtMethod newMethod, CtMethod baseMethod, StringBuilder sb) throws Exception {
		// does nothing by default, since you can override generateMethod and do it all yourself
	}
	
	protected void generateMethod(CtMethod newMethod, CtMethod baseMethod) throws Exception {
		StringBuilder sb = new StringBuilder("{");
		processBy((By)baseMethod.getAnnotation(By.class), sb);
		generate(newMethod, baseMethod, sb);
		generateReturn(newMethod, baseMethod, sb);
		sb.append("}");
		
		setBody(newMethod, sb);
	}
	
	protected void setBody(CtMethod newMethod, StringBuilder sb) throws Exception {
		System.out.println(newMethod.getName());
		System.out.println(sb.toString());
		
		newMethod.setBody(sb.toString());
	}
	
	protected void generateReturn(CtMethod newMethod, CtMethod baseMethod, StringBuilder sb) throws Exception {
		generateStandardReturn(newMethod, sb);
	}
	
	protected void processBy(By by, StringBuilder sb) {
		processBy(by, LOCAL_BY, sb);
	}
	
	protected void processBy(By by, String varName, StringBuilder sb) {
		
		if (by != null) {
			
			sb.append("org.openqa.selenium.By ").append(varName).append(" = org.openqa.selenium.By.");
			
			if (!empty(by.value())) {
				sb.append("id(byStack.resolve(\"").append(by.value()).append("\"));");
			} else if (!empty(by.id())) {
				sb.append("id(\"").append(by.id()).append("\");");
			} else if (!empty(by.className())) {
				sb.append("className(\"").append(by.className()).append("\");");
			}
		}
	}
	
	protected void generateStandardReturn(CtMethod newMethod, StringBuilder sb) throws Exception {

		assert isStandardReturn(newMethod) : "can only generate standard returns for methods declared with a standard return!";
		
		CtClass newClass = newMethod.getDeclaringClass();
		CtClass returnType = newMethod.getReturnType();

		
		if (newClass.getInterfaces()[0] == returnType) {
			sb.append("return this;");
		} else if (isPanel(returnType)) {
			sb.append("return makePanel(").append(returnType.getName()).append(".class);");
		} else if (isPage(returnType)) {
			sb.append("return navigateTo(").append(returnType.getName()).append(".class);");
		}
	}
	
	protected String makeByFromMethod(CtMethod method) throws Exception {
		By by = (By)method.getAnnotation(By.class);
		
		String result = "";
		
		if (!empty(by.value())) {
			result = "org.openqa.selenium.By.id(\"" + by.value() + "\")";
		} else if (!empty(by.id())) {
			result = "org.openqa.selenium.By.id(\"" + by.id() + "\")";
		} else if (!empty(by.className())) {
			result = "org.openqa.selenium.By.className(\"" + by.className() + "\")";
		} else {
			throw new AssertionError("NOT POSSIBLE!");
		}
		
		return result;
	}
	
	protected boolean empty(String string) {
		return string == null || string.isEmpty();
	}
}
