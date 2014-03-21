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

import java.io.IOException;
import javax.inject.Singleton;
import javax.inject.Inject;

import jj.resource.ResourceInstanceCreator;
import jj.script.AbstractScriptEnvironmentCreator;
import jj.script.ScriptEnvironmentInitializer;

/**
 * @author jason
 *
 */
@Singleton
public class DocumentScriptEnvironmentCreator extends AbstractScriptEnvironmentCreator<DocumentScriptEnvironment> {
	
	private final ResourceInstanceCreator creator;
	
	@Inject
	DocumentScriptEnvironmentCreator(final ScriptEnvironmentInitializer initializer, final ResourceInstanceCreator creator) {
		super(initializer);
		this.creator = creator;
	}

	@Override
	protected DocumentScriptEnvironment createScriptEnvironment(String name, Object... args) throws IOException {
		DocumentScriptEnvironment dse = creator.createResource(
			DocumentScriptEnvironment.class,
			cacheKey(Virtual, name),
			Virtual,
			name,
			args
		);
		
		return dse;
	}
}
