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
	
	public final String name() {
		return name;
	}

	public abstract void run() throws Exception;

}
