/*
 *    Copyright 2012 Jason Miller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jj.script;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import jj.event.Listener;
import jj.event.Subscriber;
import jj.execution.DelayedExecutor.CancelKey;
import jj.execution.TaskRunner;
import jj.script.module.RootScriptEnvironment;
import jj.util.Sequence;

/**
 * <p>
 * Provides standard timer functions to script environments, i.e.
 * <ul>
 *  <li>setTimeout
 *  <li>setInterval
 *  <li>clearTimeout
 *  <li>clearInterval
 * </ul>
 * 
 * @author jason
 *
 */
@Singleton
@Subscriber
class Timers {
	
	private static final Object[] EMPTY_SLICE = new Object[0];
	
	private final TaskRunner taskRunner;
	private final ContinuationCoordinator continuationCoordinator;
	private final CurrentScriptEnvironment env;
	
	// there is a bit of a dance around the cancel keys.  they must be stored according to the root environment, because
	// it's conceivable that a module will pass a cancel key via exports or a callback or a function call to some other
	// environment - but the timer itself should execute in the context of the original environment
	private final ConcurrentHashMap<RootScriptEnvironment, Map<String, CancelKey>> runningTimers = new ConcurrentHashMap<>();
	private final Sequence cancelIds = new Sequence();

	@Inject
	Timers(
		final TaskRunner taskRunner,
		final ContinuationCoordinator continuationCoordinator,
		final CurrentScriptEnvironment env
	) {
		this.taskRunner = taskRunner;
		this.continuationCoordinator = continuationCoordinator;
		this.env = env;
	}
	
	@Listener
	void scriptEnvironmentDied(ScriptEnvironmentDied event) {
		
		Map<String, CancelKey> cancelKeys = runningTimers.remove(event.scriptEnvironment());
		if (cancelKeys != null) {
			cancelKeys.values().forEach(CancelKey::cancel);
		}
	}
	
	private void killTimerCancelKey(final ScriptEnvironment se, final String timerKey) {
		Map<String, CancelKey> keys = runningTimers.get(se);
		if (keys != null) {
			CancelKey key = keys.remove(timerKey);
			if (key != null) {
				key.cancel();
			}
		}
	}
	
	private String setTimer(final Callable function, final int delay, final boolean repeat, final Object...args) {
		
		final String key = "jj-timer-" + cancelIds.next();
		final ScriptEnvironment rootEnvironment = env.currentRootScriptEnvironment();
		
		ScriptTask<ScriptEnvironment> task =
			new ScriptTask<ScriptEnvironment>(repeat ? "setInterval" : "setTimeout", env.current()) {
				@Override
				protected void begin() throws Exception {
					// if this is setTimeout, kill the cancelation structure
					if (!repeat) {
						killTimerCancelKey(rootEnvironment, key);
					}
					
					pendingKey = continuationCoordinator.execute(scriptEnvironment, function, args);
				}
				
				@Override
				protected void complete() throws Exception {
					// we need to repeat once the task is complete, as an artifact of the resumable structure
					// to do otherwise would require a way to clone tasks, which should actually be doable?
					// but for now, repeat on complete
					if (repeat) {
						repeat();
					}
				}
				
				@Override
				protected long delay() {
					return delay;
				}
			};
		
		taskRunner.execute(task);
		
		runningTimers.computeIfAbsent(env.currentRootScriptEnvironment(), a -> new HashMap<>()).put(key, task.cancelKey());
		
		return key;
	}
	
	private final class TimerFunction extends BaseFunction {

		private static final long serialVersionUID = 1L;
		
		private final boolean repeat;
		
		TimerFunction(final boolean repeat) {
			this.repeat = repeat;
		}
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			Callable c = null;
			int time = 0;
			Object[] slice = EMPTY_SLICE;
			
			if (args.length > 0 && args[0] instanceof Callable) {
				c = (Callable)args[0];
			}
			
			if (args.length > 1) {
				Integer timeAtt = Util.toJavaInt(args[1]);
				if (timeAtt != null) {
					time = timeAtt.intValue();
				}
			}
			
			if (args.length > 2) {
				slice = Arrays.copyOfRange(args, 2, args.length);
			}
			
			if (c != null) {
				return setTimer(c, time, repeat, slice);
			}
			
			return Undefined.instance;
		}
	}
	
	public final BaseFunction setInterval = new TimerFunction(true);
	
	public final BaseFunction setTimeout = new TimerFunction(false);
	
	public final BaseFunction clearInterval = new BaseFunction() {

		private static final long serialVersionUID = 1L;
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length > 0 && args[0] instanceof CharSequence) {
				killTimerCancelKey(env.currentRootScriptEnvironment(), String.valueOf(args[0]));
			}
			return Undefined.instance;
		}
	};
	
	public final BaseFunction clearTimeout = clearInterval;
}
