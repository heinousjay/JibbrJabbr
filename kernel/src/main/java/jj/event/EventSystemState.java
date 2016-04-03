package jj.event;

import java.lang.invoke.MethodHandle;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * Shared state for the event system. The {@link EventListenerBinder}
 * uses the add methods, and the publisher digs through the state to
 * find invocations, and the cleaner listens to the reference queue
 *
 * Created by jasonmiller on 4/2/16.
 */
class EventSystemState {

	/** a helper to keep the streams looking neat */
	private static final ConcurrentMap<Class<?>, MethodHandle> EMPTY_MAP = new ConcurrentHashMap<>(0);

	/** a helper to keep the streams looking neat */
	private static final ConcurrentLinkedQueue<WeakReference<Object>> EMPTY_QUEUE = new ConcurrentLinkedQueue<>();

	/** maps event type => (receiver type => handle) */
	private final ConcurrentMap<Class<?>, ConcurrentMap<Class<?>, MethodHandle>> handleByReceiverTypeByEventType =
		new ConcurrentHashMap<>(1024, 0.75f, 2);

	/** maps receiver type => [instance] */
	final ConcurrentMap<Class<?>, ConcurrentLinkedQueue<WeakReference<Object>>> instancesByReceiverType =
		new ConcurrentHashMap<>(1024, 0.75f, 2);

	/** keeps the queue for cleanup */
	final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();

	/**
	 * Adds a method handle to be used for listener invocation
	 * @param targetEventType the event type this handle listens for
	 * @param handle the handle that listens
	 */
	void addHandle(Class<?> targetEventType, MethodHandle handle) {
		handleByReceiverTypeByEventType.computeIfAbsent(
			handle.type().parameterType(1),
			h -> new ConcurrentHashMap<>(4, 0.75f, 2)
		).put(targetEventType, handle);
	}

	/**
	 * add an instance of a subscriber
	 * @param instance the subscriber instance
	 */
	void addListenerInstance(Object instance) {
		instancesByReceiverType.computeIfAbsent(
			instance.getClass(),
			type -> new ConcurrentLinkedQueue()
		).add(new WeakReference<>(instance, referenceQueue));
	}

	/**
	 * returns the mapping of receiver type => method handle for a given event type
	 * @param eventType the interesting event type
	 * @return a possibly empty map of receiver type => method handle
	 */
	ConcurrentMap<Class<?>, MethodHandle> handleByReceiverTypesFor(Class<?> eventType) {
		return handleByReceiverTypeByEventType.getOrDefault(eventType, EMPTY_MAP);
	}

	/**
	 * returns the queue of instance references for a given receiver type
	 * @param receiverType the type of receiver
	 * @return a possibly empty queue of references to receiver instances
	 */
	ConcurrentLinkedQueue<WeakReference<Object>> instancesFor(Class<?> receiverType) {
		return instancesByReceiverType.getOrDefault(receiverType, EMPTY_QUEUE);
	}

	/**
	 * count of all registered, live listener references. this information is
	 * immediately out of date, and is only exported for testing
	 * @return the count
	 */
	int totalListeners() {
		return instancesByReceiverType.values().stream()
			.map(ConcurrentLinkedQueue::size)
			.reduce((total, count) -> total + count)
			.orElse(0);
	}

	/**
	 * count of all registered, live listeners for a given event type. This
	 * information is immediately out of date and is only exposed for
	 * testing
	 * @param eventType the interesting event type
	 * @return the count
	 */
	int listenerCountFor(Class<?> eventType) {
		return handleByReceiverTypesFor(eventType).keySet().stream()
			.map(receiverType -> instancesFor(receiverType).size())
			.reduce((total, count) -> total + count)
			.orElse(0);
	}

	@Override
	public String toString() {
		return handleByReceiverTypeByEventType + "\n" + instancesByReceiverType;
	}
}
