package jj.event;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * <p>
 * Upon encountering a new type in the injector, introspects that
 * type's methods for event listeners and binds them with dynamic
 * invocations.
 *
 * <p>
 * This listener is expected to only be bound to classes annotated with
 * {@link Subscriber}
 *
 * Created by jasonmiller on 4/1/16.
 */
class EventListenerBinder implements TypeListener {

	private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

	/**
	 * Helper to turn the method object into a method handle
	 * @param m the reflected method
	 * @return the method handle
	 */
	private static MethodHandle unreflect(Method m) {
		try {
			m.setAccessible(true);
			return lookup.unreflect(m);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	private final EventSystemState state;

	EventListenerBinder(EventSystemState state) {
		this.state = state;
	}

	@Override
	public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {

		final Class<?> targetType = type.getRawType();

		AtomicBoolean foundListener = new AtomicBoolean();

		// grabs all the @Listener methods from the target type hierarchy
		// maps them to MethodHandles, then stores them in the state
		// structure so they can be found later
		listenerMethodStream(targetType)
			.map(EventListenerBinder::unreflect)
			.forEach(handle -> {
				foundListener.set(true);
				state.addHandle(targetType, handle);
			});

		if (!foundListener.get()) {
			encounter.addError("%s is annotated as a @Subscriber but has no @Listener methods", targetType.getName());
		} else {
			// if we found listener methods, then we need to track instances of the subscriber class
			encounter.register((InjectionListener<I>) state::addListenerInstance);
		}
	}

	/**
	 * makes a stream over all the methods of the given class
	 * and its superclasses that are annotated as a listener and
	 * are not private or static and also take a single parameter.
	 */
	private Stream<Method> listenerMethodStream(Class<?> c) {

		assert c != null : "no class supplied!";

		Stream<Method> methods = null;
		do {
			Stream<Method> stream = Arrays.stream(c.getDeclaredMethods())
				.filter(m ->
					// we want all methods that are annotated properly,
					// not private or static, and that take exactly one
					// parameter that is not of type Object
					m.getAnnotation(Listener.class) != null &&
					!Modifier.isStatic(m.getModifiers()) &&
					!Modifier.isPrivate(m.getModifiers()) &&
					m.getParameterTypes().length == 1 &&
					m.getParameterTypes()[0] != Object.class
				);
			methods = methods == null ? stream : Stream.concat(methods, stream);
		} while ((c = c.getSuperclass()) != Object.class);

		return methods;
	}
}
