package jj.execution;

/**
 * The interface to task execution in the system
 * @author jason
 *
 */
public interface TaskRunner {
	
	Promise execute(final JJTask task);

}