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
package jj.webdriver.panel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.webdriver.Page;
import jj.webdriver.Panel;
import jj.webdriver.URL;

import com.google.inject.Injector;

/**
 * produces page objects according to
 * 
 * @author jason
 *
 */
@Singleton
public class PanelFactory {
	
	private static final Map<Class<? extends Panel>, Class<? extends Panel>> implementations = new HashMap<>();
	
	private static final String INJECT_ANNOTATION = "javax.inject.Inject";
	
	private final ClassPool classPool = ClassPool.getDefault();
	
	private final CtClass panelBase;
	
	private final CtConstructor panelBaseCtor;
	
	private final Injector injector;
	
	private final Set<PanelMethodGenerator> generators;
	
	@Inject
	PanelFactory(
		final Injector injector,
		final Set<PanelMethodGenerator> generators,
		final Class<? extends PanelBase> baseClass
	) throws Exception {
		panelBase = classPool.get(baseClass.getName());
		panelBaseCtor = panelBase.getConstructors()[0];
		this.injector = injector;
		this.generators = generators;
	}
	
	/**
	 * creates a page instance by type.  it is expected that the connected browser is already
	 * on the configured URL, and this might even get asserted!
	 * @param panelInterface
	 * @return
	 * @throws Exception
	 */
	public <T extends Panel> T create(final Class<T> panelInterface) {
		
		assert panelInterface != null : "provide a panel interface";
		assert panelInterface.isInterface() : "panels are produced only from interfaces";
		assert !Page.class.isAssignableFrom(panelInterface) || panelInterface.isAnnotationPresent(URL.class) : "page interfaces must have URI annotations";
		
		try {
			if (!implementations.containsKey(panelInterface)) {
				implementations.put(panelInterface, define(panelInterface));
			}
			
			Panel instance = injector.getInstance(implementations.get(panelInterface));
			
			return panelInterface.cast(instance);
			
		} catch (Exception e) {
			throw new AssertionError("could not generate " + panelInterface.getName(), e);
		}
	}
	
	private <T extends Panel> Class<T> define(Class<? super T> panelInterface) throws Exception {
		
		CtClass ctClass = classPool.makeClass(makeClassName(panelInterface), panelBase);
		CtClass panelCtClass = classPool.get(panelInterface.getName());
		ctClass.addInterface(panelCtClass);
		
		try {
			
			prepareForInjection(ctClass);
			
			defineMethods(ctClass, panelCtClass);
			
			@SuppressWarnings("unchecked")
			Class<T> result = (Class<T>)ctClass.toClass();
			
			return result;
			
		} finally {
			
			// not going to need this anymore
			ctClass.detach();
			// but we keep the page class around because it
			// could be a return type for a method and it'll
			// just get recreated
		}
	}
	
	private String makeClassName(Class<?> panelInterface) {
		return getClass().getPackage().getName() + ".GeneratedImplementationFor$$" + panelInterface.getName().replace('.', '_') + "$$";
	}
	
	private void prepareForInjection(CtClass ctClass) throws Exception {
		CtConstructor ctor = CtNewConstructor.copy(panelBaseCtor, ctClass, null);
		
		ctor.setBody("super($$);");
		ctClass.addConstructor(ctor);

		ConstPool constpool = ctClass.getClassFile().getConstPool();
		
		AnnotationsAttribute attribute = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
		Annotation annotation = new Annotation(INJECT_ANNOTATION, constpool);
		attribute.addAnnotation(annotation);
		ctor.getMethodInfo().addAttribute(attribute);
	}
	
	private void defineMethods(CtClass ctClass, CtClass panelCtClass) throws Exception {
		
		for (CtMethod baseMethod : panelCtClass.getDeclaredMethods()) {
			CtMethod newMethod = 
				new CtMethod(baseMethod.getReturnType(), baseMethod.getName(), baseMethod.getParameterTypes(), ctClass);
			
			PanelMethodGenerator generator = findGenerator(newMethod, baseMethod);
			
			try {
			
				generator.generateMethod(newMethod, baseMethod);
			
				ctClass.addMethod(newMethod);

			} catch (Exception e) {
				throw new AssertionError(
					"generator " + generator.getClass().getName() + 
					" failed to generate " + newMethod.getDeclaringClass().getInterfaces()[0].getName() + "." + newMethod.getName(),
					e
				);
			}
		}
	}
	
	private PanelMethodGenerator findGenerator(CtMethod newMethod, CtMethod baseMethod) throws Exception {
		
		for (PanelMethodGenerator generator : generators) {
			if (generator.matches(newMethod, baseMethod)) {
				return generator;
			}
		}
		
		throw new AssertionError("no generator found for " + baseMethod.getDeclaringClass().getName() + "." + baseMethod.getName());
	}
}
