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
package jj.resource.script.environment;

import static org.mockito.BDDMockito.given;

import org.mockito.Mock;

import jj.execution.MockJJExecutors;
import jj.resource.ResourceBase;
import jj.resource.ResourceFinder;
import jj.resource.ResourceMaker;
import jj.resource.html.HtmlResource;

/**
 * @author jason
 *
 */
public class DocumentScriptEnvironmentCreatorTest extends ResourceBase<DocumentScriptEnvironment, DocumentScriptEnvironmentCreator> {

	ResourceMaker resourceMaker;
	@Mock ResourceFinder resourceFinder;
	MockJJExecutors executors = new MockJJExecutors();

	@Override
	protected String baseName() {
		return "index";
	}

	@Override
	protected DocumentScriptEnvironment resource() throws Exception {
		
		resourceMaker = new ResourceMaker(configuration);
		HtmlResource htmlResource = resourceMaker.makeHtml("index");
		given(resourceFinder.loadResource(HtmlResource.class, "index")).willReturn(htmlResource);
		
		DocumentScriptEnvironment result = new DocumentScriptEnvironment(cacheKey(), baseName(), resourceFinder, executors);
		
		executors.executor.runUntilIdle();
		
		return result;
	}

	@Override
	protected DocumentScriptEnvironmentCreator toTest() {
		return new DocumentScriptEnvironmentCreator(creator);
	}

}
