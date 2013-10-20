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

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import javax.inject.Singleton;
import javax.inject.Inject;

import jj.resource.AbstractResourceCreator;
import jj.resource.ResourceInstanceCreator;

/**
 * @author jason
 *
 */
@Singleton
public class DocumentScriptEnvironmentCreator extends AbstractResourceCreator<DocumentScriptEnvironment> {
	
	private final ResourceInstanceCreator creator;
	
	@Inject
	DocumentScriptEnvironmentCreator(final ResourceInstanceCreator creator) {
		this.creator = creator;
	}

	@Override
	public Class<DocumentScriptEnvironment> type() {
		return DocumentScriptEnvironment.class;
	}

	@Override
	public boolean canLoad(String name, Object... args) {
		return false;
	}

	@Override
	public DocumentScriptEnvironment create(String baseName, Object... args) throws IOException {
		return creator.createResource(DocumentScriptEnvironment.class, cacheKey(baseName), baseName, Paths.get("/"), args);
	}
	
	@Override
	protected URI uri(String baseName, Object... args) {
		// this escapes it for us
		return Paths.get(baseName).toUri();
	}

}
