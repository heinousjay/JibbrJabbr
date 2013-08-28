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

import java.util.HashSet;

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

/**
 * @author jason
 * 
 */
@Singleton
class ConfigurationClassLoader extends ClassLoader {
	
	private static final String SINGLETON_ANNOTATION = "javax.inject.Singleton";
	private static final String INJECT_ANNOTATION = "javax.inject.Inject";
	private static final String NAME_FORMAT = "%sGeneratedImplementationFor%s%s";

	static {
		registerAsParallelCapable();
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
		
		/*
		// @Singleton (just in case!)
		attribute = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
		annotation = new Annotation(SINGLETON_ANNOTATION, constpool);
		attribute.addAnnotation(annotation);
		ccFile.addAttribute(attribute);
		*/
	}
	
	private void implement(final CtClass result, final CtClass resultInterface) throws Exception {
		
		HashSet<String> scriptMethodNames = new HashSet<>();
		result.addInterface(resultInterface);
		for (CtMethod method : resultInterface.getDeclaredMethods()) {
			CtMethod newMethod = CtNewMethod.copy(method, result, null);
			Argument argumentAnnotation = (Argument)method.getAnnotation(Argument.class);
			Default defaultAnnotation = (Default)method.getAnnotation(Default.class);
			String defaultValue = defaultAnnotation != null ? "\"" + defaultAnnotation.value() + "\"" : null;
			if (argumentAnnotation != null) {
				String body = 
					"return ($r)readArgument(\"" +
					argumentAnnotation.value() +
					"\"," +
					defaultValue +
					"," +
					method.getReturnType().getName() +
					".class);";
				newMethod.setBody(body);
			} else { // it's assumed to come from script
				scriptMethodNames.add(newMethod.getName());
				String body = 
					"return ($r)readScriptValue(\"" +
					newMethod.getName() +
					"\"," +
					defaultValue +
					"," +
					method.getReturnType().getName() +
					".class);";
				newMethod.setBody(body);
			}
			result.addMethod(newMethod);
		}
		
		createConfigureScriptObjectMethod(result, scriptMethodNames);
	}

	private void createConfigureScriptObjectMethod(final CtClass result, HashSet<String> scriptMethodNames) throws Exception {
		
		StringBuilder newMethod = 
			new StringBuilder("protected org.mozilla.javascript.Scriptable configureScriptObject(org.mozilla.javascript.Scriptable scope) {")
			.append("org.mozilla.javascript.Scriptable result = null;")
			.append("jj.script.RhinoContext context = contextMaker.context();")
			.append("try {result = context.newObject(scope);} finally {context.close();}");
		
		for (String name : scriptMethodNames) {
			newMethod.append("org.mozilla.javascript.ScriptableObject.putConstProperty(result,\"")
				.append(name)
				.append("\",configurationFunction(\"")
				.append(name)
				.append("\"));");
		}
		
		newMethod.append("return result;}");
		result.addMethod(CtNewMethod.make(newMethod.toString(), result));
	}
}
