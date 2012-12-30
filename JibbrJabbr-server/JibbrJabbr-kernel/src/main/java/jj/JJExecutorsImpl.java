package jj;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import jj.script.ScriptExecutorFactory;
import jj.script.ScriptRunner;

/**
 * convenience object to pack up all the methods
 * of execution available inside JibbrJabbr, so
 * that dependencies can be simpler and more easily
 * mockable.  uses interface/impl separation and 
 * the picocontainer HIDE_IMPL characteristic because
 * this component is used to break a lot of dependency
 * cycles.  The script runner is kinda central to
 * everything, but at the same time it also relies
 * on pretty much the whole system to get the job
 * done
 * 
 * @author jason
 *
 */
class JJExecutorsImpl implements JJExecutors {

	private final ScriptRunner scriptRunner;
	private final IOExecutor ioExecutor;
	private final ScriptExecutorFactory scriptExecutorFactory;
	
	public JJExecutorsImpl(
		final ScriptRunner scriptRunner,
		final IOExecutor ioExecutor,
		final ScriptExecutorFactory scriptExecutorFactory
	) {
		this.scriptRunner = scriptRunner;
		this.ioExecutor = ioExecutor;
		this.scriptExecutorFactory = scriptExecutorFactory;
	}
	
	public ScriptRunner scriptRunner() {
		return scriptRunner;
	}
	
	public ExecutorService ioExecutor() {
		return ioExecutor;
	}
	
	public ScriptExecutorFactory scriptExecutorFactory() {
		return scriptExecutorFactory;
	}
	
	// conveniences to keep method call chains down

	public ScheduledExecutorService scriptExecutorFor(final String baseName) {
		return scriptExecutorFactory.executorFor(baseName);
	}
	
	public boolean isScriptThread() {
		return scriptExecutorFactory.isScriptThread();
	}

	public boolean isIOThread() {
		return ioExecutor.isIOThread();
	}
	
	public int ioPoolSize() {
		return ioExecutor.getMaximumPoolSize();
	}
}