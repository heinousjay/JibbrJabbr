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

import static jj.util.CodeGenHelper.*;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.util.CodeGenHelper;

/**
 * @author jason
 * 
 */
@Singleton
class ConfigurationClassLoader extends ClassLoader {
	
	private static final String INJECT_ANNOTATION = "javax.inject.Inject";
	private static final String SINGLETON_ANNOTATION = "javax.inject.Singleton";
	private static final String NAME_FORMAT = "%s$$GeneratedImplementation$$%s";
	
	
	static {
		registerAsParallelCapable();
	}
	
	private final ClassPool classPool;
	
	private final CtClass[] constructionParameters;
	
	private final CtClass[] constructionExceptions = new CtClass[0];
	
	@Inject
	ConfigurationClassLoader() throws Exception {
		super(ConfigurationClassLoader.class.getClassLoader());
		classPool = CodeGenHelper.classPool();
		constructionParameters = new CtClass[] { 
			classPool.get(ConfigurationCollector.class.getName())
		};
	}
	
	<T> Class<? extends T> makeConfigurationClassFor(Class<T> configurationInterface) throws Exception {
		
		CtClass resultInterface = classPool.get(configurationInterface.getName());
		
		final String name = String.format(NAME_FORMAT,
			configurationInterface.getName(),
			configurationInterface.hashCode()
		);
		
		CtClass result = classPool.makeClass(name);
		result.addInterface(resultInterface);
		
		// make it!
		prepareConfigurationClassForInjection(result);
		implementConfigurationClass(result, resultInterface);
		
		byte[] b = result.toBytecode();
		
		// no need to keep these around
		result.detach();
		resultInterface.detach();
		
		@SuppressWarnings("unchecked")
		Class<? extends T> resultClass = (Class<? extends T>)defineClass(name, b, 0, b.length);
		return resultClass;
	}
	
	private void prepareConfigurationClassForInjection(final CtClass result) throws CannotCompileException {
		
		CtField collectorField = CtField.make("private final " + ConfigurationCollector.class.getName() + " collector;", result);
		result.addField(collectorField);
		
		CtConstructor ctor = CtNewConstructor.make(constructionParameters, constructionExceptions, result);
		ctor.setBody(
			"{" +
				"this.collector = $1;" +
			"}"
		);
		result.addConstructor(ctor);
		
		ClassFile ccFile = result.getClassFile();
		ConstPool constpool = ccFile.getConstPool();
		
		// @Inject
		AnnotationsAttribute inject = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
		Annotation injectAnnotation = new Annotation(INJECT_ANNOTATION, constpool);
		inject.addAnnotation(injectAnnotation);
		ctor.getMethodInfo().addAttribute(inject);
		
		// @Singleton
		AnnotationsAttribute singleton = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
		Annotation singletonAnnotation = new Annotation(SINGLETON_ANNOTATION, constpool);
		singleton.addAnnotation(singletonAnnotation);
		ccFile.addAttribute(singleton);
	}
	
	private void implementConfigurationClass(final CtClass result, final CtClass resultInterface) throws Exception {
		

		for (CtMethod method : resultInterface.getDeclaredMethods()) {
			CtMethod newMethod = CtNewMethod.copy(method, result, null);
			
			Default defaultAnnotation = (Default)method.getAnnotation(Default.class);
			String defaultValue = defaultAnnotation != null ? "\"" + defaultAnnotation.value() + "\"" : null;
			
			String name = resultInterface.getName() + "." + newMethod.getName();
			
			if (method.getReturnType().isPrimitive()) {
				
				String returnType = method.getReturnType().getName();
				String type = primitiveNamesToWrappers.get(returnType).getName();
				
				newMethod.setBody(
					"{" +
						"Object value = collector.get(\"" + name + "\", " + type + ".class, " + defaultValue + ");" +
						"if (value == null) { return " + primitiveDefaults.get(type) + "; }" +
						"else { return ($r)value; }" +
					"}"
				);
				
			} else {
				
				newMethod.setBody(
					"{" +
						"return ($r)collector.get(\"" + name + "\", " + method.getReturnType().getName() + ".class, " + defaultValue + ");" +
					"}"
				);
			}
			
			result.addMethod(newMethod);
		}
		
	}
}
