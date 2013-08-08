package jj.configuration;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * The central point of configuration for the system
 * @author jason
 *
 */
@Singleton
public class Configuration {
	
	private final ClassPool classPool = new ClassPool(true);
	
	private final CtClass abstractConfiguration = classPool.get(AbstractConfiguration.class.getName());
	
	private final Path appPath;
	
	@Inject
	Configuration(final String[] args) throws Exception {
		
		Set<String> argSet = new HashSet<>(Arrays.asList(args));
		
		appPath = appPath(argSet);
	}
	
	// exactly one arg MUST be a path to the directory
	// containing what we serve
	private Path appPath(final Set<String> args) {
		Path result = null;
		for (String arg : args) {
			try {
				Path candidate = Paths.get(arg);
				if (Files.isDirectory(candidate, LinkOption.NOFOLLOW_LINKS)) {
					result = candidate;
				}
			} catch (Exception e) {}
		}
		if (result != null) {
			args.remove(result.toString());
		}
		return result;
	}
	
	public <T> T get(Class<T> configurationClass) throws Exception {
		assert configurationClass != null : "configuration class cannot be null";
		// maybe i loosen this to abstract class for mix-in purposes?
		assert configurationClass.isInterface() : "configuration class must be an interface";
		
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
		implement(result, resultInterface);
		
		return configurationClass.cast(result.toClass().newInstance());
	}
	
	private void implement(final CtClass result, final CtClass resultInterface) throws Exception {
		result.addInterface(resultInterface);
		for (CtMethod method : resultInterface.getDeclaredMethods()) {
			CtMethod newMethod = CtNewMethod.copy(method, result, null);
			newMethod.setBody("return path();");
			result.addMethod(newMethod);
		}
	}
	
	/**
	 * the base path from which we serve.
	 * @return
	 */
	public Path appPath() {
		return appPath;
	}
	
	/**
	 * Flag indicating that the client should be in debug mode, which
	 * will log internal info to the script console
	 * @return
	 */
	public boolean debugClient() {
		return false;
	}
	
	public boolean isSystemRunning() {
		return true;
	}
}
