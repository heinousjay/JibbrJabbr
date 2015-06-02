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
package jj.document.servable;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.http.server.ServableResource;
import jj.http.server.uri.URIMatch;
import jj.resource.ServableLoader;

import org.jsoup.nodes.Element;

/**
 * @author jason
 *
 */
@Singleton
class ResourceUrlDocumentFilter implements DocumentFilter {
	
	private static final String URI_PREPEND = "http://fairly.highly.unlikely";
	
	private static final String HREF = "href";
	private static final String SRC = "src";
	private static final String SELECTOR = "[" + HREF + "],[" + SRC + "]";
	
	private final ServableLoader servableLoader;
	
	@Inject
	ResourceUrlDocumentFilter(ServableLoader servableLoader) {
		this.servableLoader = servableLoader;
	}

	@Override
	public boolean needsIO(final DocumentRequestProcessor documentRequestProcessor) {
		return true;
	}
	
	private String massageURL(final String url) {
		if (url.startsWith(URI_PREPEND)) {
			String path = url.substring(URI_PREPEND.length());
			URIMatch uriMatch = new URIMatch(path);
			ServableResource resource = uriMatch.versioned ? null : servableLoader.loadResource(uriMatch);
			if (resource != null) {
				return resource.serverPath();
			}
			return path;
		}
		return url;
	}

	@Override
	public void filter(final DocumentRequestProcessor documentRequestProcessor) {
		documentRequestProcessor.document().setBaseUri(URI_PREPEND + documentRequestProcessor.uri());
		for (Element el : documentRequestProcessor.document().select(SELECTOR)) {
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
