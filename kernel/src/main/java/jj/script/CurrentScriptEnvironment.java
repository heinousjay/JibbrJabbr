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

import jj.CurrentResource;
import jj.jjmessage.JJMessage;

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
	 * Prepares a continuation and throws it.  Returns the ContinuationPending
	 * so that callers can write
	 * <code>throw context.prepareContinuation(...)</code>
	 * so the compiler stays happy, but this method never returns normally
	 * 
	 * the result will return a string
	 * 
	 * @param jjMessage
	 * @return
	 */
	public ContinuationPending prepareContinuation(JJMessage jjMessage) {
		ContinuationPending result = prepareContinuation(new ContinuationState(jjMessage));
		String pendingKey = current().continuationPending(result);
		jjMessage.pendingKey(pendingKey);
		throw result;
	}
	
	/**
	 * Prepares and throws a continuation for a rest request
	 * @param restRequest
	 * @return
	 */
	public ContinuationPending prepareContinuation(RestRequest restRequest) {
		ContinuationPending result = prepareContinuation(new ContinuationState(restRequest));
		String pendingKey = current().continuationPending(result);
		restRequest.pendingKey(pendingKey);
		throw result;
	}
	
	/**
	 * prepares and throws a continuation to require a new module
	 * @param require
	 * @return
	 */
	public ContinuationPending prepareContinuation(RequiredModule require) {
		ContinuationPending result = prepareContinuation(new ContinuationState(require));
		String pendingKey = current().continuationPending(result);
		require.pendingKey(pendingKey); // = new key!
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
}
