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
package jj.resource.script;

import jj.StringUtils;
import jj.script.Continuation;
import jj.script.ContinuationPendingKey;
import jj.script.ScriptEnvironment;

/**
 * @author jason
 *
 */
public class RequiredModule implements Continuation {
	
	private final ScriptEnvironment parent;
	
	private final String identifier;
	
	private final String toString;
	
	private ContinuationPendingKey pendingKey;

	public RequiredModule(
		final ScriptEnvironment parent,
		final String identifier
	) {
		assert parent != null : "no required module without a parent!";
		assert !StringUtils.isEmpty(identifier) : "no required module without an identifier!";
		this.parent = parent;
		this.identifier = identifier;
		this.toString = "required module " + identifier + " under " + parent.name();
	}
	
	String identifier() {
		return identifier;
	}
	
	ScriptEnvironment parent() {
		return parent;
	}
	
	@Override
	public ContinuationPendingKey pendingKey() {
		assert pendingKey != null;
		return pendingKey;
	}
	
	@Override
	public void pendingKey(ContinuationPendingKey pendingKey) {
		assert this.pendingKey == null;
		assert pendingKey != null;
		this.pendingKey = pendingKey;
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof RequiredModule) {
			RequiredModule other = (RequiredModule)obj;
			result = other.parent == this.parent && other.identifier.equals(identifier);
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		return toString.hashCode();
	}
	
	@Override
	public String toString() {
		return toString;
	}
}
