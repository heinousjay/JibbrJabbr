package jj.configuration;

import io.netty.util.internal.PlatformDependent;

import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.CoreConfiguration;

import com.google.inject.Injector;

/**
 * The central point of configuration for the system
 * @author jason
 *
 */
@Singleton
public class Configuration {
	
	private final ConcurrentMap<Class<?>, ConfigurationObjectBase> configurationInterfaceToImplementation =
		PlatformDependent.newConcurrentHashMap();
	
	private final ConfigurationClassLoader classLoader;
	
	private final Injector injector;
	
	@Inject
	Configuration(
		final ConfigurationClassLoader classLoader,
		final Injector injector
	) throws Exception {
		this.classLoader = classLoader;
		this.injector = injector;
	}
	
	public <T> T get(final Class<T> configurationClass) {
		assert configurationClass != null : "configuration class cannot be null";
		// maybe i loosen this to abstract class for mix-in purposes?
		assert configurationClass.isInterface() : "configuration class must be an interface";
		
		ConfigurationObjectBase configurationInstance = configurationInterfaceToImplementation.get(configurationClass);
		if (configurationInstance == null) {
			try {
				configurationInstance = injector.getInstance(classLoader.makeClassFor(configurationClass));
				if (configurationInstance != null) {
					configurationInterfaceToImplementation.put(configurationClass, configurationInstance);
				}
			} catch (Exception e) {
				throw new AssertionError(e);
			}
		}
		// special case, the core configuration has no scripted properties
		// in fact, i'm moving the app path back inside, i think, and maybe killing
		// the argument thing?  i don't know
		if (!CoreConfiguration.class.isAssignableFrom(configurationClass)) {
			configurationInstance.runScriptFunction();
		}
		
		return configurationClass.cast(configurationInstance);
	}
	
	public boolean isSystemRunning() {
		return true;
	}
}
