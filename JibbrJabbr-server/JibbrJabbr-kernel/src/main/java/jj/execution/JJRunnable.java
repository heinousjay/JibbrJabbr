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
	public void run() {
		try {
			doRun();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AssertionError("exception travelled to JJRunnable! BAD", e);
		}
	}

	public void doRun() {}
	
	@Override
	public final String toString() {
		return name;
	}
}
