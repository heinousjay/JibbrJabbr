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
package jj.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.StringUtils;

/**
 * @author jason
 * 
 */
@Singleton
class ConfigurationClassLoader extends ClassLoader {
	
	private static final String INJECT_ANNOTATION = "javax.inject.Inject";
	private static final String NAME_FORMAT = "%sGeneratedImplementationFor%s%s";
	private static final Map<String, String> primitiveDefaults;
	
	static {
		registerAsParallelCapable();
		
		Map<String, String> builder = new HashMap<>();
		builder.put("boolean", "false");
		builder.put("char", "(char)0");
		builder.put("byte", "(byte)0");
		builder.put("short", "(short)0");
		builder.put("int", "(int)0");
		builder.put("long", "(long)0");
		builder.put("float", "(float)0");
		builder.put("double", "(double)0");
		
		primitiveDefaults = Collections.unmodifiableMap(builder);
	}
	
	private final ClassPool classPool = new ClassPool(true);
	
	private final CtClass abstractConfiguration = classPool.get(ConfigurationObjectBase.class.getName());
	
	@Inject
	ConfigurationClassLoader() throws Exception {}

	@SuppressWarnings("unchecked")
	Class<? extends ConfigurationObjectBase> makeClassFor(Class<?> configurationClass) throws Exception {

		CtClass resultInterface = classPool.get(configurationClass.getName());
		
		final String name = String.format(NAME_FORMAT,
			Configuration.class.getName(),
			configurationClass.getSimpleName(),
			Integer.toHexString(configurationClass.hashCode())
		);
		
		CtClass result = classPool.makeClass(name, abstractConfiguration);
		
		prepareForInjection(result);
		implement(result, resultInterface);
		
		byte[] b = result.toBytecode();
		
		// no need to keep these around
		result.detach();
		resultInterface.detach();
		
		return (Class<? extends ConfigurationObjectBase>)defineClass(name, b, 0, b.length);
	}
	
	private void prepareForInjection(final CtClass result) throws CannotCompileException {
		CtConstructor ctor = CtNewConstructor.copy(abstractConfiguration.getConstructors()[0], result, null);
		ctor.setBody("super($$);");
		result.addConstructor(ctor);
		
		ClassFile ccFile = result.getClassFile();
		ConstPool constpool = ccFile.getConstPool();
		
		// @Inject
		AnnotationsAttribute attribute = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
		Annotation annotation = new Annotation(INJECT_ANNOTATION, constpool);
		attribute.addAnnotation(annotation);
		ctor.getMethodInfo().addAttribute(attribute);
	}
	
	private void implement(final CtClass result, final CtClass resultInterface) throws Exception {
		
		List<String> scriptProps = new ArrayList<>();
		List<String> defaults = new ArrayList<>();
		result.addInterface(resultInterface);
		for (CtMethod method : resultInterface.getDeclaredMethods()) {
			CtMethod newMethod = CtNewMethod.copy(method, result, null);
			Method methodAnnotation = (Method)method.getAnnotation(Method.class);
			boolean allArgs = methodAnnotation != null && methodAnnotation.allArgs();
			
			Default defaultAnnotation = (Default)method.getAnnotation(Default.class);
			String defaultValue = defaultAnnotation != null ? "\"" + defaultAnnotation.value() + "\"" : null;
			
			String name = newMethod.getName();
			String functionName = methodAnnotation != null && !StringUtils.isEmpty(methodAnnotation.name()) ?
				methodAnnotation.name() : name;
			
			// these will generally be basic and repeated, so
			// keeping them is okay
			CtClass returnType = newMethod.getReturnType();
			
			scriptProps.add(new StringBuilder()
				.append("org.mozilla.javascript.ScriptableObject.putConstProperty(result,\"")
				.append(functionName)
				.append("\",configurationFunction(\"")
				.append(name)
				.append("\", ")
				.append(returnType.getName())
				.append(".class, ")
				.append(allArgs)
				.append(",")
				.append(defaultValue)
				.append("));").toString()
			);
			
			defaults.add(new StringBuilder()
				.append("values.put(\"")
				.append(name)
				.append("\", converters.convert(")
				.append(defaultValue)
				.append(",")
				.append(returnType.getName())
				.append(".class));")
				.toString()
			);
			
			if (returnType.isArray()) {
				newMethod.setBody(
					"{ java.util.ArrayList list = (java.util.ArrayList)values.get(\"" + name +"\");"+
					"if (list == null) { return new " + returnType.getComponentType().getName() + "[0]; }" +
					"return ($r)list.toArray(new " + returnType.getComponentType().getName() + "[list.size()]); }" 
				);
			} else if (returnType.isPrimitive()) {
				newMethod.setBody("{ Object value = values.get(\"" + name + "\");" +
					"if (value == null) { return "  + primitiveDefaults.get(returnType.getName()) + "; }" +
					"return ($r)value;}"
				);
			} else {
				newMethod.setBody("return ($r)values.get(\"" + name + "\");");
			}
			
			result.addMethod(newMethod);
		}
		
		createConfigureScriptObjectMethod(result, scriptProps);
		createSetDefaultsMethod(result, defaults);
	}
	
	private void createSetDefaultsMethod(final CtClass result, final List<String> defaults) throws Exception {
		StringBuilder newMethod = new StringBuilder("protected void setDefaults() {");

		for (String defaultSetting : defaults) {
			newMethod.append(defaultSetting);
		}
		
		newMethod.append("}");
		result.addMethod(CtNewMethod.make(newMethod.toString(), result));
	}

	private void createConfigureScriptObjectMethod(final CtClass result, final List<String> scriptProps) throws Exception {
		
		StringBuilder newMethod = 
			new StringBuilder("protected org.mozilla.javascript.Scriptable configureScriptObject(org.mozilla.javascript.Scriptable scope) {")
			.append("org.mozilla.javascript.Scriptable result = null;")
			.append("jj.script.RhinoContext context = contextMaker.context();")
			.append("try {result = context.newObject(scope);} finally {context.close();}");
		
		for (String prop : scriptProps) {
			newMethod.append(prop);
		}
		
		newMethod.append("return result;}");
		result.addMethod(CtNewMethod.make(newMethod.toString(), result));
	}
}
