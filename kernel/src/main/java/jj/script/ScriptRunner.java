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

import jj.http.server.JJWebSocketConnection;
import jj.http.server.servable.document.DocumentRequestProcessor;
import jj.resource.script.ModuleScriptEnvironment;

/**
 * @author jason
 *
 */
public interface ScriptRunner {

	public static final String READY_FUNCTION_KEY = "Document.ready";

	void submit(DocumentRequestProcessor documentRequestProcessor);

	void submit(RequiredModule requiredModule, ModuleScriptEnvironment scriptExecutionEnvironment);

	void submitPendingResult(JJWebSocketConnection connection, String pendingKey, Object result);

	void submit(JJWebSocketConnection connection, String event, Object... args);
}