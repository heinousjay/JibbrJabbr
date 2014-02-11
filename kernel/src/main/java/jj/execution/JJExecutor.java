package jj.execution;

import java.util.concurrent.Future;

import jj.script.ContinuationPendingKey;
import jj.script.ScriptEnvironment;

public interface JJExecutor {
	
	void resume(final ContinuationPendingKey pendingKey, final Object result);
	
	Future<?> execute(final JJTask task);
	
	boolean isScriptThread();

	boolean isScriptThreadFor(ScriptEnvironment scriptEnvironment);

	boolean isIOThread();

	int ioPoolSize();

}