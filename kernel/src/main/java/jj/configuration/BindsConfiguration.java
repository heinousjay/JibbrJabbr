package jj.configuration;

import com.google.inject.Injector;
import com.google.inject.Provider;
import jj.HasBinder;

import javax.inject.Inject;

/**
 * Mix-in to bind configuration interfaces
 *
 * Created by jasonmiller on 4/3/16.
 */
public interface BindsConfiguration extends HasBinder {

	default <T> void bindConfiguration(Class<T> configurationInterface) {
		assert configurationInterface.isInterface() : "configuration objects must be interfaces";

		Provider<T> provider = new Provider<T>() {

			@Inject ConfigurationObjectImplementation configurationObjectImplementation;
			@Inject Injector injector;
			@Override
			public T get() {
				return injector.getInstance(configurationObjectImplementation.implementationClassFor(configurationInterface));
			}
		};

		binder().requestInjection(provider);

		binder().bind(configurationInterface).toProvider(provider);
	}
}
