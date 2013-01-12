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

import jj.Sequence;

/**
 * @author jason
 *
 */
public class RequiredModule {
	
	private static final Sequence pendingKeys = new Sequence();
	
	private final String identifier;
	
	private final String baseName;
	
	private final ScriptContext parentContext;
	
	private final String pendingKey = pendingKeys.next();

	public RequiredModule(final String identifier, final CurrentScriptContext context) {
		this.identifier = identifier;
		this.baseName = context.baseName();
		this.parentContext = context.save();
	}
	
	String baseName() {
		return baseName;
	}
	
	ScriptContext parentContext() {
		return parentContext;
	}
	
	String identifier() {
		return identifier;
	}
	
	String pendingKey() {
		return pendingKey;
	}
	
	@Override
	public String toString() {
		return "required module " + pendingKey;
	}
}