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
package jj.module;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jj.html.HTMLFragment;

/**
 * Represents a resource for a module.  Should only be manipulated within the context
 * of the Module worker.
 * @author jason
 *
 */
final class Resource {
	
	private static final Pattern PATH_MAKER = Pattern.compile("^(?:file:|jar:[^!]+!)(?:assets/)?(.*)$"); 
	
	Resource(final URI uri) {
		
		assert (uri != null) : "uri must be provided";
		
		this.uri = uri;
		
		type = ResourceType.fromURI(uri);
		// follow the path segments back until we reach either a directory we recognize or
		// the root/! separator and treat that as the base path for this resource
		
		Matcher m = PATH_MAKER.matcher(uri.toString());
		m.matches();
		resourcePath = m.group(1);
	}
	
	final URI uri;
	
	/**
	 * temporarily always UTF-8
	 */
	final Charset charset = StandardCharsets.UTF_8;

	final String resourcePath;
	
	final ResourceType type;
	
	ByteBuffer bytes;
	
	/**
	 * possibly populated, if the resource type deems it to be so
	 */
	HTMLFragment htmlFragment;
	
	void process() {
		if (type != null) {
			type.processResource(this);
		}
	}
	
	@Override
	public String toString() {
		return resourcePath;
	}
}