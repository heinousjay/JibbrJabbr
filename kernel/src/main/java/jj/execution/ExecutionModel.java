package jj.execution;

import java.util.concurrent.TimeUnit;

/**
 * A description of task execution, which controls how
 * runnables are scheduled,
 *
 * @author jason
 */
public abstract class ExecutionModel {

	protected abstract void schedule(Runnable runnable, long delay, TimeUnit unit);
}
