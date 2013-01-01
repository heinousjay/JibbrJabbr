package jj;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import jj.script.ScriptExecutorFactory;
import jj.script.ScriptRunner;

public interface JJExecutors {

	ScriptRunner scriptRunner();
	
	ScheduledExecutorService httpControlExecutor();

	ExecutorService ioExecutor();

	ScriptExecutorFactory scriptExecutorFactory();

	ScheduledExecutorService scriptExecutorFor(String baseName);

	boolean isScriptThread();

	boolean isIOThread();

	int ioPoolSize();

}