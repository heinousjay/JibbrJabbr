package jj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.event.Publisher;

@Singleton
public class JJServerLifecycle {

	private final Set<JJServerStartupListener> startupListeners;
	private final Publisher publisher;
	private final Version version;
	
	@Inject
	JJServerLifecycle(
		final Set<JJServerStartupListener> startupListeners,
		final Publisher publisher,
		final Version version
	) {
		this.startupListeners = startupListeners;
		this.publisher = publisher;
		this.version = version;
	}
	
	public void start() throws Exception {
		publisher.publish(new ServerStartingEvent(version));
		// can't do this in the constructor because Guice gets unhappy about that.
		// and we can't just let the event do it yet.  but soon!
		ArrayList<JJServerStartupListener> listeners = new ArrayList<>(startupListeners);
		Collections.sort(listeners, new Comparator<JJServerStartupListener>() {

			@Override
			public int compare(JJServerStartupListener l1, JJServerStartupListener l2) {
				return l1.startPriority().compareTo(l2.startPriority());
			}
		});
		for (JJServerStartupListener listener: listeners) {
			listener.start();
		}
	}
	
	public void stop() {
		publisher.publish(new ServerStoppingEvent());
	}
}
