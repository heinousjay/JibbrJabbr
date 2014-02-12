package jj.execution;

import java.util.concurrent.Future;

import jj.script.ContinuationPendingKey;
import jj.script.ScriptEnvironment;

/**
 * The interface to task execution in the system
 * @author jason
 *
 */
public interface JJExecutor {
	
	void resume(final ContinuationPendingKey pendingKey, final Object result);
	
	Future<?> execute(final JJTask task);

	boolean isScriptThreadFor(ScriptEnvironment scriptEnvironment);

	boolean isIOThread();

}