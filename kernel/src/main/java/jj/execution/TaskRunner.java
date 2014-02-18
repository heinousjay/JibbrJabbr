package jj.execution;

/**
 * <p>
 * The API to task execution in the system
 * 
 * <p>
 * Tasks are of specific subtypes and have different execution profiles
 * defined by their contributing subsystem.  The execution system natively
 * provides the {@link ServerTask} which can be used to execute operations
 * such as monitoring loops or delayed checks.
 * 
 * <p>
 * The {@link Promise} returned from {@link #execute(JJTask)} can be used to
 * schedule another task upon completion of the original task.  This scheduling
 * will occur regardless of the finishing state of the task. There is no general
 * method provided for carrying information between the two, you must use
 * a method of your own device.
 * 
 * @author jason
 *
 */
public interface TaskRunner {
	
	Promise execute(final JJTask task);

}