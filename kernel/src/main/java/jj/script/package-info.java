/**
 * <p>The JibbrJabbr interface to the Rhino scripting engine</p> 
 * 
 * <p>
 * Supports pluggable execution with continuations via two key
 * APIs, which provide plug points at either end of script
 * execution.  Hand a {@link ScriptEnvironment}, and some other
 * parameters, to the {@link ContinuationCoordinator} and
 * watch the sparks fly!
 * 
 * <p>
 * The ScriptEnvironment API encompasses and guides all aspects 
 * of execution in the engine.  A ScriptEnvironment instance is
 * based on one or more {@link ScriptResource} instances. It can also
 * participate in the execution lifecycle by providing the scope
 * chain.
 * 
 * <p>
 * All ScriptEnvironment execution is single threaded and
 * fully asynchronous by default.
 * 
 * <p>
 * The Continuation API is the hook into the back end of script
 * execution.  when something that would block occurs, the current
 * execution is paused and stored, and a {@link ContinuationProcessor}
 * registered for the Continuation type is notified.  You activate
 * a continuation by calling {@link CurrentScriptEnvironment#preparedContinuation(Continuation)}
 * with your Continuation instance, generally from some host object.
 * 
 * <p>
 * The ContinuationProcessor will
 * then be notified with the Continuation instance, and it can do
 * as it needs to, restarting the execution by passing the
 * {@link ContinuationPendingKey} that was assigned ({@link Continuation#pendingKey()} to
 * {@link JJExecutor#resume(ContinuationPendingKey, Object)}, along
 * with whatever result should be used as the return value of the
 * continued function.
 * 
 * <p>
 * Easy Peasy.  There's one more piece - ScriptTask, which supports simple restartable
 * execution via the {@link JJExecutor} API.  That's gonna move in here soon!
 * 
 * <p>
 * And of course, if you just want to use Rhino, inject the RhinoContext
 * and go to town.
 */
package jj.script;

import jj.execution.JJExecutor;
import jj.resource.script.ScriptResource;