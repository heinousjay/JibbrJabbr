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

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.ResourceFinder;
import jj.resource.StaticResource;
import jj.servable.URIMatch;

import org.jsoup.nodes.Element;

/**
 * @author jason
 *
 */
@Singleton
class ResourceUrlDocumentFilter implements DocumentFilter {
	
	private static final String HREF = "href";
	private static final String SRC = "src";
	private static final String SELECTOR = "[" + HREF + "],[" + SRC + "]";
	
	private final ResourceFinder resourceFinder;
	
	@Inject
	ResourceUrlDocumentFilter(final ResourceFinder resourceFinder) {
		this.resourceFinder = resourceFinder;
	}

	@Override
	public boolean needsIO(DocumentRequest documentRequest) {
		return true;
	}
	
	private String massageURL(String url) {
		URIMatch uriMatch = new URIMatch(url);
		
		StaticResource resource = resourceFinder.loadResource(StaticResource.class, uriMatch.baseName);
		if (resource != null && uriMatch.sha == null) {
			return resource.uri();
		}
		
		return url;
	}

	@Override
	public void filter(DocumentRequest documentRequest) {
		for(Element el : documentRequest.document().select(SELECTOR)) {
			
			if (el.hasAttr(HREF)) {
				el.attr(HREF, massageURL(el.attr(HREF)));
			}
			
			if (el.hasAttr(SRC)) {
				el.attr(SRC, massageURL(el.attr(SRC)));
			}
		}
	}

}
