package jj.execution;

import java.util.concurrent.Future;
import jj.script.ScriptRunner;

public interface JJExecutors {
	
	Future<Void> execute(final JJTask task);

	ScriptRunner scriptRunner();

	boolean isScriptThreadFor(String baseName);

	boolean isIOThread();

	int ioPoolSize();

}