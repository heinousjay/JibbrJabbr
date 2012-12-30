package jj;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.picocontainer.Behavior;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.Injector;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.injectors.ConstructorInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class JJComponentMonitor implements ComponentMonitor {
	
	private static final Logger log = LoggerFactory.getLogger(JJComponentMonitor.class);
	
	@Override
	public Object noComponentFound(MutablePicoContainer container, Object componentKey) {
		//log.error("no component found for {}", componentKey);
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Injector newInjector(Injector injector) {
		if (injector instanceof ConstructorInjector) {
			((ConstructorInjector<?>)injector).withNonPublicConstructors();
		}
		return injector;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Behavior newBehavior(Behavior behavior) {
		return behavior;
	}

	@Override
	public void lifecycleInvocationFailed(MutablePicoContainer container,
			ComponentAdapter<?> componentAdapter, Method method,
			Object instance, RuntimeException cause) {
		// we don't lifecycle
	}

	@Override
	public Object invoking(PicoContainer container,
			ComponentAdapter<?> componentAdapter, Member member,
			Object instance, Object[] args) {
		// not sure what this is supposed to do.  thanks docs!
		return instance;
	}

	@Override
	public void invoked(PicoContainer container,
			ComponentAdapter<?> componentAdapter, Member member,
			Object instance, long duration, Object[] args, Object retVal) {
		// we could log something here?
	}

	@Override
	public void invocationFailed(Member member, Object instance, Exception cause) {
		// we could log something here
	}

	@Override
	public <T> void instantiationFailed(PicoContainer container,
			ComponentAdapter<T> componentAdapter, Constructor<T> constructor,
			Exception cause) {
		log.error("couldn't instantiate using {}", constructor);
		log.error("", cause);
		throw new AssertionError("could not build a server");
	}

	@Override
	public <T> Constructor<T> instantiating(PicoContainer container,
			ComponentAdapter<T> componentAdapter, Constructor<T> constructor) {
		constructor.setAccessible(true);
		return constructor;
	}

	@Override
	public <T> void instantiated(PicoContainer container,
			ComponentAdapter<T> componentAdapter, Constructor<T> constructor,
			Object instantiated, Object[] injected, long duration) {
		
	}
}