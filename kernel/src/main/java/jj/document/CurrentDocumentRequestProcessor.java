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
package jj.document;

import javax.inject.Singleton;

import org.jsoup.nodes.Document;

import jj.document.servable.DocumentRequestProcessor;
import jj.execution.ExecutionInstance;

/**
 * exposes the current DocumentRequestProcessor being processed as a resource
 * so the rest of the system can get to it
 * @author jason
 *
 */
@Singleton
public class CurrentDocumentRequestProcessor extends ExecutionInstance<DocumentRequestProcessor> {

	
	public Document currentDocument() {
		return current() == null ? null : current().document();
	}
}
