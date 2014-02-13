package jj.execution;

import java.util.concurrent.Future;

/**
 * The interface to task execution in the system
 * @author jason
 *
 */
public interface TaskRunner {
	
	Future<?> execute(final JJTask task);

	boolean isIOThread();

}