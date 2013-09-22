package jj.execution;

import java.util.concurrent.Future;

public interface JJExecutors {
	
	Future<Void> execute(final JJTask task);
	
	boolean isScriptThread();

	boolean isScriptThreadFor(String baseName);

	boolean isIOThread();

	int ioPoolSize();

}