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
package jj.http.server.servable;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.resource.ResourceFinder;
import jj.resource.stat.ic.StaticResource;
import jj.uri.URIMatch;
import jj.http.HttpRequest;
import jj.http.HttpResponse;

/**
 * @author jason
 *
 */
@Singleton
public class StaticServable extends Servable<StaticResource> {

	private final ResourceFinder resourceFinder;
	
	/**
	 * @param configuration
	 */
	@Inject
	protected StaticServable(
		final Configuration configuration,
		final ResourceFinder resourceFinder
	) {
		super(configuration);
		this.resourceFinder = resourceFinder;
	}

	/**
	 * we always (potentially) match
	 */
	@Override
	public boolean isMatchingRequest(final URIMatch uriMatch) {
		return true;
	}

	@Override
	public RequestProcessor makeRequestProcessor(
		final HttpRequest request,
		final HttpResponse response
	) throws IOException {
		final StaticResource resource = loadResource(request.uriMatch());
		if (resource != null && isServablePath(resource.path())) {
			return makeStandardRequestProcessor(request, response, request.uriMatch(), resource);
		}
		
		return null;
		
	}

	@Override
	public StaticResource loadResource(URIMatch match) {
		return resourceFinder.loadResource(StaticResource.class, match.baseName);
	}

	@Override
	public Class<StaticResource> type() {
		return StaticResource.class;
	}

}
