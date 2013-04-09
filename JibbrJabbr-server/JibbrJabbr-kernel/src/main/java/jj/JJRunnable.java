package jj;

/**
 * The way to run a task
 * 
 * @author jason
 *
 */
public abstract class JJRunnable {
	
	private final String name;
	protected JJRunnable(final String name) {
		this.name = name;
	}
	
	protected boolean ignoreInExecutionTrace() {
		return false;
	}

	public abstract void run() throws Exception;
	
	@Override
	public final String toString() {
		return name;
	}

}
