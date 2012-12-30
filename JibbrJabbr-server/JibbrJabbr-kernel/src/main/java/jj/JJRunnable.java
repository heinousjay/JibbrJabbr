package jj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JJRunnable implements Runnable {

	private final Logger log = LoggerFactory.getLogger(JJRunnable.class);
	
	private final String taskName;
	
	public JJRunnable(final String taskName) {
		this.taskName = taskName;
	}
	
	@Override
	public final void run() {
		try {
			innerRun();
		} catch (OutOfMemoryError rethrow) {
			throw rethrow;
		} catch (Throwable t) {
			log.error("Problem running a task {}", taskName);
			log.error("", t);
		}
	}
	
	protected abstract void innerRun() throws Exception;

}
