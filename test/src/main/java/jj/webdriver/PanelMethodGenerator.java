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

import java.util.Arrays;
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
	
	protected boolean hasBy(CtMethod baseMethod) throws Exception {
		return baseMethod.hasAnnotation(By.class) && new ByReader((By)baseMethod.getAnnotation(By.class)) != null;
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
	
	protected int sliceAt() {
		return 0;
	}
	
	protected void generateMethod(CtMethod newMethod, CtMethod baseMethod) throws Exception {
		StringBuilder sb = new StringBuilder("{");
		processBy((By)baseMethod.getAnnotation(By.class), sliceAt(), sb);
		generate(newMethod, baseMethod, sb);
		generateReturn(newMethod, baseMethod, sb);
		sb.append("}");
		
		setBody(newMethod, sb);
	}
	
	protected void setBody(CtMethod newMethod, StringBuilder sb) throws Exception {
		// log it! but don't have the right loggers yet
		System.out.print(newMethod.getName());
		System.out.print(" = ");
		System.out.println(sb.toString());
		
		newMethod.setBody(sb.toString());
	}
	
	protected void generateReturn(CtMethod newMethod, CtMethod baseMethod, StringBuilder sb) throws Exception {
		generateStandardReturn(newMethod, sb);
	}
	
	/**
	 * process the {@link By} annotation into a local variable named by {@link #LOCAL_BY}, without passing
	 * along the method args as format parameters.
	 * @param by
	 * @param sb
	 */
	protected void processBy(By by, int sliceArgs, StringBuilder sb) {
		processBy(by, LOCAL_BY, sliceArgs, sb);
	}
	
	protected void processBy(By by, String varName, int sliceArgs, StringBuilder sb) {
		
		if (by != null) {
			ByReader br = new ByReader(by);
			
			// args might get sliced for an implementation
			if (sliceArgs > -1) {
				sb.append("Object[] slicedArgs = java.util.Arrays.copyOfRange($args, ").append(sliceArgs).append(", $args.length);");
			}
			
			sb.append("org.openqa.selenium.By ").append(varName).append(" = org.openqa.selenium.By.");
			sb.append(br.type()).append("(");

			if (br.needsResolution()) {
				sb.append("byStack.resolve(");
			}
			
			if (sliceArgs > -1) {
				sb.append("String.format(");
			}
			
			sb.append("\"").append(br.value()).append("\"");
			
			if (sliceArgs > -1) {
				sb.append(", slicedArgs)");
			}
			
			if (br.needsResolution()) {
				sb.append(")");
			}
			
			sb.append(");");
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
	
	protected boolean empty(String string) {
		return string == null || string.isEmpty();
	}

	protected boolean parametersMatchByAnnotation(int sliceAt, CtMethod newMethod, CtMethod baseMethod) throws Exception {
		
		By by = (By)baseMethod.getAnnotation(By.class);
		
		int length = baseMethod.getParameterTypes().length;
		CtClass[] params = Arrays.copyOfRange(baseMethod.getParameterTypes(), sliceAt, length);
		Object[] args = new Object[params.length];
		

		boolean result = args.length == 0 || (by != null && args.length > 0);
		
		int index = 0;
		for (CtClass type : params) {
			if ("java.lang.String".equals(type.getName())) {
				args[index++] = " ";
			} else if ("int".equals(type.getName())) {
				args[index++] = 0;
			} else {
				System.out.println("unknown type! " + type.getName());
				result = false;
			}
		}
		
		result = result && (by == null || new ByReader(by).validateValueAsFormatterFor(args));
		
		return result;
	}
}
