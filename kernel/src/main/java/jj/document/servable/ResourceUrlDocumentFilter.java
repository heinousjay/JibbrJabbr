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

import static io.netty.handler.codec.http.HttpMethod.GET;
import javax.inject.Inject;
import javax.inject.Singleton;

import jj.http.server.ServableResource;
import jj.http.server.ServableResources;
import jj.http.server.uri.RouteMatch;
import jj.http.server.uri.Router;
import jj.http.server.uri.URIMatch;
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
	
	private final ServableResources servables;
	private final Router router;
	
	@Inject
	ResourceUrlDocumentFilter(
		final ServableResources servables,
		final Router router
	) {
		this.servables = servables;
		this.router = router;
	}

	@Override
	public boolean needsIO(final DocumentRequestProcessor documentRequestProcessor) {
		return true;
	}
	
	private String massageURL(final String url) {
		if (url.startsWith(URI_PREPEND)) {
			String path = url.substring(URI_PREPEND.length());
			URIMatch uriMatch = new URIMatch(path);
			if (!uriMatch.versioned && uriMatch.path != null) {
				
				RouteMatch match = router.routeRequest(GET, uriMatch);
				if (match.matched()) {
					Class<? extends ServableResource> resourceClass = servables.classFor(match.resourceName());
					ServableResource resource = 
						servables.routeProcessor(match.resourceName()).loadResource(resourceClass, uriMatch, match.route());
					if (resource != null && uriMatch.sha1 == null) {
						return resource.serverPath();
					}
				}
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
