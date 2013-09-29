package jj.execution;

import java.util.concurrent.Future;

public interface JJExecutors {
	
	Future<?> execute(final JJTask task);
	
	boolean isScriptThread();

	boolean isScriptThreadFor(String baseName);

	boolean isIOThread();

	int ioPoolSize();

}