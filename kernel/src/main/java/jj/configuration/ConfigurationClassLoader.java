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

import java.util.ArrayList;
import java.util.List;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.Annotation;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.util.CodeGenHelper;
import jj.util.StringUtils;

/**
 * @author jason
 * 
 */
@Singleton
class ConfigurationClassLoader extends ClassLoader {
	
	private static final String INJECT_ANNOTATION = "javax.inject.Inject";
	private static final String NAME_FORMAT = "%sGeneratedImplementationFor%s%s";
	
	
	static {
		registerAsParallelCapable();
	}
	
	private final ClassPool classPool;
	
	private final CtClass[] constructionParameters;
	
	private final CtClass[] constructionExceptions = new CtClass[0];
	
	private final CtClass configBase;
	
	private final CtConstructor configBaseCtor;
	
	private final AttributeInfo configBaseCtorSignature;
	
	@Inject
	ConfigurationClassLoader() throws Exception {
		super(ConfigurationClassLoader.class.getClassLoader());
		classPool = CodeGenHelper.classPool();
		constructionParameters = new CtClass[] { 
			classPool.get(ConfigurationCollector.class.getName())
		};
		configBase = classPool.get(ConfigurationObjectBase.class.getName());
		configBaseCtor = configBase.getConstructors()[0];
		configBaseCtorSignature = configBaseCtor.getMethodInfo().getAttribute(SignatureAttribute.tag);
	}
	
	<T> Class<? extends T> makeConfigurationClassFor(Class<T> configurationInterface) throws Exception {
		
		CtClass resultInterface = classPool.get(configurationInterface.getName());
		
		final String name = String.format(NAME_FORMAT,
			Configuration.class.getName(),
			configurationInterface.getSimpleName(),
			Integer.toHexString(configurationInterface.hashCode())
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
		AnnotationsAttribute attribute = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
		Annotation annotation = new Annotation(INJECT_ANNOTATION, constpool);
		attribute.addAnnotation(annotation);
		ctor.getMethodInfo().addAttribute(attribute);
	}
	
	private void implementConfigurationClass(final CtClass result, final CtClass resultInterface) throws Exception {
		

		for (CtMethod method : resultInterface.getDeclaredMethods()) {
			CtMethod newMethod = CtNewMethod.copy(method, result, null);
			
			Default defaultAnnotation = (Default)method.getAnnotation(Default.class);
			String defaultValue = defaultAnnotation != null ? "\"" + defaultAnnotation.value() + "\"" : null;
			
			String name = resultInterface.getName() + "." + newMethod.getName();
			
			Class<?> returnType = method.getReturnType().isPrimitive() ?
				primitiveNamesToWrappers.get(method.getReturnType().getName()) :
				Class.forName(method.getReturnType().getName());
			
			newMethod.setBody(
				"{" +
					"return ($r)collector.get(\"" + name + "\", " + returnType.getName() + ".class, " + defaultValue + ");" +
				"}"
			);
			
			result.addMethod(newMethod);
		}
		
	}

	@SuppressWarnings("unchecked")
	Class<? extends ConfigurationObjectBase> makeClassFor(Class<?> configurationClass) throws Exception {

		CtClass resultInterface = classPool.get(configurationClass.getName());
		
		final String name = String.format(NAME_FORMAT,
			Configuration.class.getName(),
			configurationClass.getSimpleName(),
			Integer.toHexString(configurationClass.hashCode())
		);
		
		CtClass result = classPool.makeClass(name, configBase);
		
		prepareForInjection(result);
		implement(result, resultInterface);
		
		byte[] b = result.toBytecode();
		
		// no need to keep these around
		result.detach();
		resultInterface.detach();
		
		return (Class<? extends ConfigurationObjectBase>)defineClass(name, b, 0, b.length);
	}
	
	private void prepareForInjection(final CtClass result) throws CannotCompileException {
		CtConstructor ctor = CtNewConstructor.copy(configBaseCtor, result, null);
		
		// need to add the generic information to the constructor
		MethodInfo ctorInfo = ctor.getMethodInfo();
		ctorInfo.addAttribute(configBaseCtorSignature.copy(ctorInfo.getConstPool(), null));
		
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
			.append("jj.script.RhinoContext context = (jj.script.RhinoContext)contextProvider.get();")
			.append("try {result = context.newObject(scope);} finally {context.close();}");
		
		for (String prop : scriptProps) {
			newMethod.append(prop);
		}
		
		newMethod.append("return result;}");
		result.addMethod(CtNewMethod.make(newMethod.toString(), result));
	}
}
