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
package jj.resource.document;

import static org.mockito.BDDMockito.*;

import org.mockito.Mock;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import jj.engine.EngineAPI;
import jj.event.Publisher;
import jj.http.server.CurrentWebSocketConnection;
import jj.resource.ResourceBase;
import jj.resource.ResourceFinder;
import jj.resource.ResourceMaker;
import jj.resource.document.DocumentScriptEnvironment;
import jj.resource.document.DocumentScriptEnvironmentCreator;
import jj.resource.document.HtmlResource;
import jj.resource.script.ScriptResource;
import jj.resource.script.ScriptResourceType;
import jj.script.MockRhinoContextProvider;

/**
 * this test only validates that the creator is getting all of the necessary stuff to the resource. there needs to
 * be a separate test for creating a resource to validate it works as expected
 * 
 * @author jason
 *
 */
public class DocumentScriptEnvironmentCreatorTest extends ResourceBase<DocumentScriptEnvironment, DocumentScriptEnvironmentCreator> {

	ResourceMaker resourceMaker;
	@Mock ResourceFinder resourceFinder;
	@Mock EngineAPI api;
	@Mock ScriptableObject local;
	@Mock Publisher publisher;
	@Mock ScriptCompiler compiler;
	@Mock DocumentWebSocketMessageProcessors processors;
	MockRhinoContextProvider contextProvider;
	

	@Override
	protected String baseName() {
		return "index";
	}
	
	private void givenMinimalServices() throws Exception {
		resourceMaker = new ResourceMaker(configuration);
		
		contextProvider = new MockRhinoContextProvider();
		given(contextProvider.context.newObject(any(Scriptable.class))).willReturn(local);
	}
	
	private void givenDocumentScriptEnvironmentResources(String baseName) throws Exception {
		HtmlResource htmlResource = resourceMaker.makeHtml(HtmlResourceCreator.resourceName(baseName));
		given(resourceFinder.loadResource(HtmlResource.class, HtmlResourceCreator.resourceName(baseName))).willReturn(htmlResource);
		
		ScriptResource serverResource = resourceMaker.makeScript(ScriptResourceType.Server.suffix(baseName));
		given(resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Server.suffix(baseName))).willReturn(serverResource);
	}

	@Override
	protected DocumentScriptEnvironment resource() throws Exception {
		
		givenMinimalServices();
		givenDocumentScriptEnvironmentResources(baseName());
		
		DocumentScriptEnvironment result = 
			new DocumentScriptEnvironment(
				cacheKey(),
				baseName(),
				resourceFinder,
				contextProvider,
				api,
				publisher,
				compiler,
				processors,
				new CurrentDocument(),
				new CurrentWebSocketConnection()
			);
		
		return result;
	}


	@Override
	protected DocumentScriptEnvironmentCreator toTest() {
		return new DocumentScriptEnvironmentCreator(creator);
	}

}
