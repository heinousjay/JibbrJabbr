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
package jj.script.module;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.charset.Charset;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Provider;

import org.mozilla.javascript.ScriptableObject;

import jj.resource.AbstractFileResource;
import jj.script.Global;
import jj.script.RhinoContext;

/**
 * Represents a file containing a JSON serialization, exposes the deserialized value
 * as {@link #contents()}
 * 
 * @author jason
 *
 */
public class JSONResource extends AbstractFileResource<Void> {
	
	private final String source;
	
	private final Object contents;

	@Inject
	JSONResource(
		final Dependencies dependencies,
		final Path path,
		final @Global ScriptableObject global,
		final Provider<RhinoContext> contextProvider
	) throws Exception {
		super(dependencies, path);
		source = byteBuffer.toString(UTF_8); // by rule
		
		try (RhinoContext context = contextProvider.get()) {
			contents = context.newJsonParser(global).parseValue(source);
		}
	}

	public String source() {
		return source;
	}
	
	public Object contents() {
		return contents;
	}
	
	@Override
	public Charset charset() {
		return UTF_8;
	}
}
