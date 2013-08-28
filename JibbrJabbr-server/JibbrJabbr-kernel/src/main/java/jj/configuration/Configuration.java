package jj.configuration;

import io.netty.util.internal.PlatformDependent;

import java.nio.file.Path;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.conversion.Converters;

import com.google.inject.Injector;

/**
 * The central point of configuration for the system
 * @author jason
 *
 */
@Singleton
public class Configuration {
	
	/**
	 * 
	 */
	private static final String APP_PATH_ARG_NAME = "app";

	private final ConcurrentMap<Class<?>, Class<? extends ConfigurationObjectBase>> configurationInterfaceToImplementation =
		PlatformDependent.newConcurrentHashMap();
	
	private final Arguments arguments;
	
	private final Converters converters;
	
	private final ConfigurationClassLoader classLoader;
	
	private final Injector injector;
	
	@Inject
	Configuration(
		final Arguments arguments,
		final Converters converters,
		final ConfigurationClassLoader classLoader,
		final Injector injector
	) {
		this.arguments = arguments;
		this.converters = converters;
		this.classLoader = classLoader;
		this.injector = injector;
	}
	
	public <T> T get(final Class<T> configurationInterface) {
		assert configurationInterface != null : "configuration class cannot be null";
		// maybe i loosen this to abstract class for mix-in purposes?
		assert configurationInterface.isInterface() : "configuration class must be an interface";
		
		Class<? extends ConfigurationObjectBase> configurationImplementation = configurationInterfaceToImplementation.get(configurationInterface);
		if (configurationImplementation == null) {
			try {
				configurationImplementation = classLoader.makeClassFor(configurationInterface);
				configurationInterfaceToImplementation.putIfAbsent(configurationInterface, configurationImplementation);
			} catch (IllegalAccessError iae) {
				throw new AssertionError(configurationInterface.getName() + " must be public!");
			} catch (LinkageError le) {
				configurationImplementation = configurationInterfaceToImplementation.get(configurationInterface);
				assert (configurationImplementation != null) : "something is screwy in the configuration maker";
			} catch (Exception e) {
				throw new AssertionError(e);
			}
		}
		
		ConfigurationObjectBase configurationInstance = injector.getInstance(configurationImplementation);

		if (configurationInstance != null) {
			configurationInstance.runScriptFunction();
		}
		
		return configurationInterface.cast(configurationInstance);
	}
	
	public Path appPath() {
		return converters.convert(arguments.get(APP_PATH_ARG_NAME), Path.class);
	}
	
	public boolean isSystemRunning() {
		return true;
	}
}
