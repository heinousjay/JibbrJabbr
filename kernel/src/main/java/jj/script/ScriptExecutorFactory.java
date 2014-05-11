package jj.script;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import jj.ServerStopping;
import jj.event.Listener;
import jj.event.Subscriber;

@Singleton
@Subscriber
class ScriptExecutorFactory {
	
	private final ScriptExecutor executor;
		
	@Inject
	ScriptExecutorFactory(final Provider<ScriptExecutor> executors) {
		this.executor = executors.get();
	}
	
	ScriptExecutor executorFor(ScriptEnvironment scriptEnvironment) {
		while (scriptEnvironment instanceof ChildScriptEnvironment) {
			scriptEnvironment = ((ChildScriptEnvironment)scriptEnvironment).parent();
		}
		
		return executor;
	}
	
	boolean isScriptThreadFor(ScriptEnvironment scriptEnvironment) {
		return executor.isScriptThread();
	}

	@Listener
	public void stop(ServerStopping event) {
		executor.shutdownNow();
	}

}
