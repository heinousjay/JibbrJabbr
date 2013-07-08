package jj.execution;

/**
 * The way to run a task
 * 
 * @author jason
 *
 */
public abstract class JJRunnable implements Runnable {
	
	private final String name;
	protected JJRunnable(final String name) {
		this.name = name;
	}
	
	protected boolean ignoreInExecutionTrace() {
		return false;
	}
	
	@Override
	public final void run() {
		try {
			doRun();
		} catch (Exception e) {
			throw new AssertionError("exception travelled to JJRunnable.  catch this earlier!", e);
		}
	}

	public abstract void doRun() throws Exception;
	
	@Override
	public final String toString() {
		return name;
	}

}
