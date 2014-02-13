package jj.execution;

import java.util.concurrent.Future;

import jj.script.ContinuationPendingKey;

/**
 * The interface to task execution in the system
 * @author jason
 *
 */
public interface TaskRunner {
	
	void resume(final ContinuationPendingKey pendingKey, final Object result);
	
	Future<?> execute(final JJTask task);

	boolean isIOThread();

}