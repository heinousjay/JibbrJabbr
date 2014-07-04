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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import jj.event.Subscriber;
import jj.execution.DelayedExecutor.CancelKey;
import jj.execution.TaskRunner;

/**
 * 
 * 
 * @author jason
 *
 */
@Singleton
public class Timers {
	
	private final TaskRunner taskRunner;
	private final ContinuationCoordinator continuationCoordinator;
	private final CurrentScriptEnvironment env;

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
	
	private CancelKey setTimer(final Function function, final int delay, final boolean repeat, final Object...args) {
		
		ScriptTask<ScriptEnvironment> task = 
			new ScriptTask<ScriptEnvironment>(
				repeat ? "setInterval" : "setTimeout",
				env.current(),
				continuationCoordinator
			) {
				@Override
				protected void begin() throws Exception {
					continuationCoordinator.execute(scriptEnvironment, function, args);
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
		
		return task.cancelKey();
	}
	
	public final BaseFunction setInterval = new BaseFunction() {

		private static final long serialVersionUID = 1L;
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			assert args.length >= 2;
			return setTimer((Function)args[0], Util.toJavaInt(args[1]), true, Arrays.copyOfRange(args, 2, args.length));
		}
	};
	
	public final BaseFunction setTimeout = new BaseFunction() {

		private static final long serialVersionUID = 1L;
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			assert args.length >= 2;
			return setTimer((Function)args[0], Util.toJavaInt(args[1]), false, Arrays.copyOfRange(args, 2, args.length));
		}
	};
	
	public final BaseFunction clearInterval = new BaseFunction() {

		private static final long serialVersionUID = 1L;
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length == 1 && args[0] instanceof CancelKey) {
				((CancelKey)args[0]).cancel();
			}
			return Undefined.instance;
		}
	};
	
	public final BaseFunction clearTimeout = clearInterval;
}
