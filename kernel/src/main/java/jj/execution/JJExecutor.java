package jj.execution;

import java.util.concurrent.Future;

import jj.script.ContinuationPendingKey;

public interface JJExecutor {
	
	void resume(final ContinuationPendingKey pendingKey, final Object result);
	
	Future<?> execute(final JJTask task);
	
	boolean isScriptThread();

	boolean isScriptThreadFor(String baseName);

	boolean isIOThread();

	int ioPoolSize();

}