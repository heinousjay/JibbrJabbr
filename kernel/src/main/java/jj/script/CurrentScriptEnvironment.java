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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.mozilla.javascript.ContinuationPending;

import jj.http.server.WebSocketConnectionHost;
import jj.util.Closer;
import jj.util.CurrentResource;

/**
 * 
 * 
 * @author jason
 *
 */
@Singleton
public class CurrentScriptEnvironment extends CurrentResource<ScriptEnvironment> {
	
	private final Provider<RhinoContext> contextProvider;
	
	@Inject
	CurrentScriptEnvironment(final Provider<RhinoContext> contextProvider) {
		this.contextProvider = contextProvider;
	}
	
	/**
	 * internal method, to allow continuation resumption to also involve environment-dependent
	 * context restoration
	 */
	Closer enterScope(final AbstractScriptEnvironment scriptEnvironment, final ContinuationPendingKey pendingKey) {
		
		final Closer environmentCloser = enterScope(scriptEnvironment);
		final Closer contextCloser = scriptEnvironment.restoreContextForKey(pendingKey);
		
		return new Closer() {
			
			@Override
			public void close() {
				environmentCloser.close();
				contextCloser.close();
			}
		};
	}
	
	public WebSocketConnectionHost currentWebSocketConnectionHost() {
		return current() instanceof WebSocketConnectionHost ? (WebSocketConnectionHost)current() : null;
	}
	
	protected AbstractScriptEnvironment innerCurrent() {
		return (AbstractScriptEnvironment)current();
	}
	
	/**
	 * Prepares a continuation and throws it.  Returns the ContinuationPending
	 * so that callers can write
	 * <code>throw context.prepareContinuation(...)</code>
	 * so the compiler stays happy, but this method never returns normally
	 * 
	 * the continuation will be configured to restore the state of the script environment
	 * on resumption
	 * 
	 * @param continuation
	 * @return
	 */
	public ContinuationPending preparedContinuation(Continuation continuation) {
		assert current() != null : "can't perform a continuation unless a script is in context";
		
		ContinuationPending result = prepareContinuation(new ContinuationState(continuation));
		ContinuationPendingKey pendingKey = innerCurrent().createContinuationContext(result);
		continuation.pendingKey(pendingKey);
		throw result;
	}

	/**
	 * captures a continuation from the rhino interpreter, and stores the information
	 * necessary for resumption
	 * @param pendingId 
	 * @param continuationState
	 * @return
	 */
	private ContinuationPending prepareContinuation(ContinuationState continuationState) {
		
		try(RhinoContext context = contextProvider.get()) {
			ContinuationPending continuation = context.captureContinuation();
			continuation.setApplicationState(continuationState);
			return continuation;
		} catch (Exception e) {
			throw new AssertionError("could not capture a continuation", e);
		}
	}

	/**
	 * @return
	 */
	public ScriptEnvironment currentRootScriptEnvironment() {
		ScriptEnvironment current = current();
		while (current instanceof ChildScriptEnvironment) {
			current = ((ChildScriptEnvironment)current).parent();
		}
		return current;
	}
}
