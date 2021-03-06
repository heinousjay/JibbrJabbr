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
package jj.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

/**
 * <p>
 * Some useful stuff for code gen and dealing with types generically
 * 
 * <p>
 * All codegen systems should get their {@link ClassPool} from this location. This is
 * particularly important if you want to generate classes based on other
 * generated classes (as in event subscription) because there needs to be
 * some sort of app-level storage for the generated bytes, since javassist
 * doesn't keep them, despite misleading documentation to the contrary.
 * 
 * @author jason
 *
 */
public enum CodeGenHelper {

	;
	
	/**
	 * Adds a runtime-visible annotation to the given method, which can be a constructor.
	 * The {@link Annotation} is returned if values need to be added
	 */
	public static Annotation addAnnotationToMethod(final CtBehavior method, final Class<? extends java.lang.annotation.Annotation> annotation) {
		CtClass ctClass = method.getDeclaringClass();
		ClassFile ccFile = ctClass.getClassFile();
		ConstPool constpool = ccFile.getConstPool();
		
		AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
		Annotation a = new Annotation(annotation.getName(), constpool);
		attr.addAnnotation(a);
		method.getMethodInfo().addAttribute(attr);
		
		return a;
	}

	/**
	 * Adds a runtime-visible annotation to the given class.
	 * The {@link Annotation} is returned if values need to be added
	 */
	public static Annotation addAnnotationToClass(final CtClass ctClass, final Class<? extends java.lang.annotation.Annotation> annotation) {
		ClassFile ccFile = ctClass.getClassFile();
		ConstPool constpool = ccFile.getConstPool();
		
		AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
		Annotation a = new Annotation(annotation.getName(), constpool);
		attr.addAnnotation(a);
		ccFile.addAttribute(attr);
		
		return a;
	}
	
	/**
	 * Holds on to generated class bytes. Shared across all {@link ClassPool}s in the system
	 * @author jason
	 *
	 */
	private static final class MemoryClassPath implements ClassPath {
		
		final ConcurrentMap<String, byte[]> classBytes = new ConcurrentHashMap<>();

		@Override
		public InputStream openClassfile(String classname) throws NotFoundException {
			return classBytes.containsKey(classname) ? new ByteArrayInputStream(classBytes.get(classname)) : null;
		}

		@Override
		public URL find(String classname) {
			try {
				return classBytes.containsKey(classname) ? new URL("file:/" + classname) : null;
			} catch (MalformedURLException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public void close() {
			classBytes.clear();
		}
	}
	
	private static final MemoryClassPath memoryClassPath = new MemoryClassPath();
	
	/**
	 * Stores a (presumably) generated {@link CtClass} for common access via all
	 * {@link ClassPool}s
	 */
	public static void storeGeneratedClass(CtClass clazz) throws Exception {
		clazz.stopPruning(true);
		memoryClassPath.classBytes.putIfAbsent(clazz.getName(), clazz.toBytecode());
		clazz.stopPruning(false);
	}
	
	private static final ThreadLocal<ClassPool> classPool = new ThreadLocal<ClassPool>() {
		@Override
		protected ClassPool initialValue() {
			ClassPool classPool = new ClassPool();
			classPool.appendClassPath(new LoaderClassPath(CodeGenHelper.class.getClassLoader()));
			classPool.appendClassPath(memoryClassPath);
			return classPool;
		}
	};
	
	/**
	 * Produces a {@link ClassPool} for code generation.  instances are per-thread to reduce contention
	 */
	public static ClassPool classPool() {
		return classPool.get();
	}
	
	public static final Map<Class<?>, Class<?>> wrappersToPrimitives;
	public static final Map<Class<?>, Class<?>> primitivesToWrappers;
	public static final Map<String, Class<?>> primitiveNamesToWrappers;
	public static final Map<String, String> primitiveDefaults;
	
	static {
		Map<Class<?>, Class<?>> builder = new HashMap<>();
		builder.put(Boolean.class,   Boolean.TYPE);
		builder.put(Character.class, Character.TYPE);
		builder.put(Byte.class,      Byte.TYPE);
		builder.put(Short.class,     Short.TYPE);
		builder.put(Integer.class,   Integer.TYPE);
		builder.put(Long.class,      Long.TYPE);
		builder.put(Float.class,     Float.TYPE);
		builder.put(Double.class,    Double.TYPE);
		
		wrappersToPrimitives = Collections.unmodifiableMap(builder);
		
		builder = new HashMap<>();
		for (Class<?> wrapper : wrappersToPrimitives.keySet()) {
			builder.put(wrappersToPrimitives.get(wrapper), wrapper);
		}
		
		primitivesToWrappers = Collections.unmodifiableMap(builder);
		
		Map<String, Class<?>> builder2 = new HashMap<>();
		for (Class<?> primitive : primitivesToWrappers.keySet()) {
			builder2.put(primitive.getName(), primitivesToWrappers.get(primitive));
		}
		
		primitiveNamesToWrappers = Collections.unmodifiableMap(builder2);
		
		Map<String, String> builder3 = new HashMap<>();
		builder3.put(Boolean.TYPE.getName(),   "false");
		builder3.put(Character.TYPE.getName(), "(char)0");
		builder3.put(Byte.TYPE.getName(),      "(byte)0");
		builder3.put(Short.TYPE.getName(),     "(short)0");
		builder3.put(Integer.TYPE.getName(),   "0");
		builder3.put(Long.TYPE.getName(),      "0L");
		builder3.put(Float.TYPE.getName(),     "0F");
		builder3.put(Double.TYPE.getName(),    "0.0");
		
		Map<String, String> builder4 = new HashMap<>();
		for (String type : builder3.keySet()) {
			builder4.put(primitiveNamesToWrappers.get(type).getName(), builder3.get(type));
		}
		
		builder3.putAll(builder4);
		
		primitiveDefaults = Collections.unmodifiableMap(builder3);
	}
}
