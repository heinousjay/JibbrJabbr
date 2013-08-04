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
package jj.http.server.servable.document;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.CssResource;
import jj.resource.Resource;
import jj.resource.ResourceFinder;
import jj.resource.StaticResource;
import jj.uri.URIMatch;

import org.jsoup.nodes.Element;

/**
 * @author jason
 *
 */
@Singleton
class ResourceUrlDocumentFilter implements DocumentFilter {
	
	private static final String URI_PREPEND = "http://notreal.com";
	
	private static final String HREF = "href";
	private static final String SRC = "src";
	private static final String SELECTOR = "[" + HREF + "],[" + SRC + "]";
	
	private final ResourceFinder resourceFinder;
	
	@Inject
	ResourceUrlDocumentFilter(final ResourceFinder resourceFinder) {
		this.resourceFinder = resourceFinder;
	}

	@Override
	public boolean needsIO(final DocumentRequestProcessor documentRequestProcessor) {
		return true;
	}
	
	private Resource loadResource(final URIMatch uriMatch) {
		// TODO account for less resources
		// the loading logic will need to be centralized somewhere, it's going to get spread around
		Class<? extends Resource> type = "css".equals(uriMatch.extension) ? CssResource.class : StaticResource.class;
		return resourceFinder.loadResource(type, uriMatch.baseName);
	}
	
	private String massageURL(final String url) {
		if (url.startsWith(URI_PREPEND)) {
			String path = url.substring(URI_PREPEND.length());
			URIMatch uriMatch = new URIMatch(path);
			if (!uriMatch.versioned && uriMatch.baseName != null) {
				Resource resource = loadResource(uriMatch);
				if (resource != null && uriMatch.sha1 == null) {
					return resource.uri();
				}
			}
			return path;
		}
		return url;
	}

	@Override
	public void filter(final DocumentRequestProcessor documentRequestProcessor) {
		documentRequestProcessor.document().setBaseUri(URI_PREPEND + documentRequestProcessor.uri());
		for(Element el : documentRequestProcessor.document().select(SELECTOR)) {
			if (el.hasAttr(HREF)) {
				el.attr(HREF, massageURL(el.absUrl(HREF)));
			}
			
			if (el.hasAttr(SRC)) {
				el.attr(SRC, massageURL(el.absUrl(SRC)));
			}
		}
		documentRequestProcessor.document().setBaseUri("");
	}

}
