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
	
	private final ConcurrentMap<Class<?>, Class<? extends ConfigurationObjectBase>> configurationInterfaceToImplementation =
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
	
	public <T> T get(final Class<T> configurationInterface) {
		assert configurationInterface != null : "configuration class cannot be null";
		// maybe i loosen this to abstract class for mix-in purposes?
		assert configurationInterface.isInterface() : "configuration class must be an interface";
		
		Class<? extends ConfigurationObjectBase> configurationImplementation = configurationInterfaceToImplementation.get(configurationInterface);
		if (configurationImplementation == null) {
			try {
				configurationImplementation = classLoader.makeClassFor(configurationInterface);
				configurationInterfaceToImplementation.putIfAbsent(configurationInterface, configurationImplementation);
			} catch (LinkageError le) {
				configurationImplementation = configurationInterfaceToImplementation.get(configurationInterface);
				assert (configurationImplementation != null) : "something is screwy in the configuration maker";
			} catch (Exception e) {
				throw new AssertionError(e);
			}
		}
		
		ConfigurationObjectBase configurationInstance = injector.getInstance(configurationImplementation);
		// special case, the core configuration does not support scripted properties
		if (configurationInstance != null && !CoreConfiguration.class.isAssignableFrom(configurationInterface)) {
			configurationInstance.runScriptFunction();
		}
		
		return configurationInterface.cast(configurationInstance);
	}
	
	public boolean isSystemRunning() {
		return true;
	}
}
