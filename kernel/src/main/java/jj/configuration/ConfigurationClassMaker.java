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

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.util.CodeGenHelper;

/**
 * @author jason
 * 
 */
@Singleton
class ConfigurationClassMaker {
	
	private static final String NAME_FORMAT = "jj.configuration.GeneratedImplementationFor$$%s$$%s";
	
	private final ClassPool classPool;
	
	private final CtClass configurationCollector;
	
	private final CtClass[] constructionExceptions = new CtClass[0];
	
	@Inject
	ConfigurationClassMaker() throws Exception {
		classPool = CodeGenHelper.classPool();
		configurationCollector = classPool.get(ConfigurationCollector.class.getName());
	}
	
	<T> Class<? extends T> make(Class<T> configurationInterface) throws Exception {
		
		final String name = String.format(NAME_FORMAT,
			configurationInterface.getName().replace(".", "_"),
			configurationInterface.hashCode()
		);
		
		// we may have already defined this class since we support being restarted
		
		try {
			@SuppressWarnings("unchecked")
			Class<? extends T> resultClass = (Class<? extends T>) Class.forName(name);
			return resultClass;
		} catch (ClassNotFoundException e) { /* just carry on */ }
		
		CtClass resultInterface = classPool.get(configurationInterface.getName());
		
		CtClass result = classPool.makeClass(name);
		result.addInterface(resultInterface);
		
		List<CtClass> collaborators = gatherCollaborators(resultInterface);
		
		// make it!
		prepareForInjection(result, collaborators);
		implement(result, resultInterface);
		
		try {
			@SuppressWarnings("unchecked")
			Class<? extends T> resultClass = (Class<? extends T>)result.toClass(getClass().getClassLoader(), null);
			return resultClass;
		} finally {
			// no need to keep these around
			result.detach();
			resultInterface.detach();
		}
	}
	
	private List<CtClass> gatherCollaborators(final CtClass resultInterface) throws Exception {
		
		ArrayList<CtClass> result = new ArrayList<>();
		
		for (CtMethod method : resultInterface.getMethods()) {
			if (method.hasAnnotation(DefaultProvider.class)) {
				Class<?> providerClass = ((DefaultProvider)method.getAnnotation(DefaultProvider.class)).value();
				result.add(classPool.get(providerClass.getName()));
			}
		}
		
		return result;
	}
	
	private void prepareForInjection(final CtClass result, final List<CtClass> collaborators) throws CannotCompileException {
		
		collaborators.add(0, configurationCollector);
		
		int index = 1;
		StringBuilder body = new StringBuilder("{");
		
		for (CtClass collaborator : collaborators) {
			CtField field = CtField.make("private final " + collaborator.getName() + " " + collaborator.getSimpleName() + ";", result);
			result.addField(field);
			body.append("this.").append(collaborator.getSimpleName()).append(" = $").append(index++).append(";");
		}
		
		body.append("}");
		
		
		CtConstructor ctor = CtNewConstructor.make(collaborators.toArray(new CtClass[collaborators.size()]), constructionExceptions, result);
		ctor.setBody(body.toString());
		result.addConstructor(ctor);
		
		// @Inject
		CodeGenHelper.addAnnotationToMethod(ctor, Inject.class);
		
		// @Singleton
		CodeGenHelper.addAnnotationToClass(result, Singleton.class);
	}
	
	private void implement(final CtClass result, final CtClass resultInterface) throws Exception {
		

		for (CtMethod method : resultInterface.getDeclaredMethods()) {
			CtMethod newMethod = CtNewMethod.copy(method, result, null);
			
			Default defaultAnnotation = (Default)method.getAnnotation(Default.class);
			String defaultValue = defaultAnnotation != null ? "\"" + defaultAnnotation.value() + "\"" : null;
			
			DefaultProvider defaultProviderAnnotation = (DefaultProvider)method.getAnnotation(DefaultProvider.class);
			
			if (defaultAnnotation != null && defaultProviderAnnotation != null) {
				throw new AssertionError("only one of @Default and @DefaultProvider can be declared for a given method");
			}
			
			if (defaultProviderAnnotation != null) {
				// assert the return type == the provider type
				
				
				defaultValue = defaultProviderAnnotation.value().getSimpleName() + ".get()";
			}
			
			String name = resultInterface.getName() + "." + newMethod.getName();
			
			if (method.getReturnType().isPrimitive()) {
				
				String returnType = method.getReturnType().getName();
				String type = primitiveNamesToWrappers.get(returnType).getName();
				
				newMethod.setBody(
					"{" +
						"Object value = " + configurationCollector.getSimpleName() + ".get(\"" + name + "\", " + type + ".class, " + defaultValue + ");" +
						"if (value == null) { return " + primitiveDefaults.get(type) + "; }" +
						"else { return ($r)value; }" +
					"}"
				);
				
			} else {
				
				newMethod.setBody(
					"{" +
						"return ($r)" + configurationCollector.getSimpleName() + ".get(\"" + name + "\", " + method.getReturnType().getName() + ".class, " + defaultValue + ");" +
					"}"
				);
				
				// if the return type is in the same package as the resultInterface,
				// we can detach it
			}
			
			result.addMethod(newMethod);
		}
		
	}
}
