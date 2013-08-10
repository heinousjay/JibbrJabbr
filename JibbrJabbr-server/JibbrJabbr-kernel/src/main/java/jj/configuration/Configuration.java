package jj.configuration;

import io.netty.util.internal.PlatformDependent;

import java.util.concurrent.ConcurrentMap;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Injector;

/**
 * The central point of configuration for the system
 * @author jason
 *
 */
@Singleton
public class Configuration {
	
	// this has to be static because class definitions are JVM wide
	private static final ConcurrentMap<Class<?>, Object> configurationInterfaceToImplementation =
		PlatformDependent.newConcurrentHashMap();
	
	private final ClassPool classPool = new ClassPool(true);
	
	private final CtClass abstractConfiguration = classPool.get(AbstractConfiguration.class.getName());
	
	private final Injector injector;
	
	@Inject
	Configuration(final Injector injector) throws Exception {
		this.injector = injector;
	}
	
	public <T> T get(final Class<T> configurationClass) {
		assert configurationClass != null : "configuration class cannot be null";
		// maybe i loosen this to abstract class for mix-in purposes?
		assert configurationClass.isInterface() : "configuration class must be an interface";
		
		Object configurationInstance = configurationInterfaceToImplementation.get(configurationClass);
		if (configurationInstance == null) {
			CtClass resultInterface;
			try {
				resultInterface = classPool.get(configurationClass.getName());
			} catch (NotFoundException nfe) {
				throw new AssertionError("couldn't find " + configurationClass.getName());
			} // impossible?
			
			CtClass result = classPool.makeClass(
				getClass().getName() + "$Generated$" + configurationClass.getSimpleName(),
				abstractConfiguration
			);
			
			try {
				makeCtor(result);
				implement(result, resultInterface);
				Class<?> implementedClass = result.toClass();
				configurationInstance = injector.getInstance(implementedClass);
				configurationInterfaceToImplementation.putIfAbsent(configurationClass, configurationInstance);
			} catch (CannotCompileException cce) {
				if (cce.getCause() instanceof LinkageError) {
					// someone snuck in behind us
					configurationInstance = configurationInterfaceToImplementation.get(configurationClass);
				} else {
					throw new AssertionError(cce);
				}
			} catch (Exception e) {
				throw new AssertionError(e);
			}
		}
		
		return configurationClass.cast(configurationInstance);
	}
	
	private void makeCtor(final CtClass result) throws CannotCompileException {
		CtConstructor ctor = CtNewConstructor.copy(abstractConfiguration.getConstructors()[0], result, null);
		ctor.setBody("super($1, $2);");
		result.addConstructor(ctor);
		
		ClassFile ccFile = result.getClassFile();
		ConstPool constpool = ccFile.getConstPool();
		AnnotationsAttribute attribute = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
		Annotation annotation = new Annotation("javax.inject.Inject", constpool);
		attribute.addAnnotation(annotation);
		ctor.getMethodInfo().addAttribute(attribute);
	}
	
	private void implement(final CtClass result, final CtClass resultInterface) throws Exception {
		
		result.addInterface(resultInterface);
		for (CtMethod method : resultInterface.getDeclaredMethods()) {
			CtMethod newMethod = CtNewMethod.copy(method, result, null);
			System.out.println(newMethod);
			Argument argumentAnnotation = (Argument)method.getAnnotation(Argument.class);
			Default defaultAnnotation = (Default)method.getAnnotation(Default.class);
			if (argumentAnnotation != null) {
				String defaultValue = defaultAnnotation != null ? "\"" + defaultAnnotation.value() + "\"" : null;
				String body = 
					"return ($r)readArgument(\"" +
					argumentAnnotation.value() +
					"\"," +
					defaultValue +
					"," +
					method.getReturnType().getName() +
					".class);";
				System.out.println(body);
				newMethod.setBody(body);
			}
			result.addMethod(newMethod);
		}
	}
	
	public boolean isSystemRunning() {
		return true;
	}
}
