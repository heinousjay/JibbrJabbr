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
package jj.servable;

import io.netty.handler.codec.http.HttpHeaders;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.resource.ResourceFinder;
import jj.resource.StaticResource;
import jj.uri.URIMatch;
import jj.http.JJHttpRequest;
import jj.http.JJHttpResponse;
import jj.http.RequestProcessor;

/**
 * @author jason
 *
 */
@Singleton
public class StaticServable extends Servable {

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
	public boolean isMatchingRequest(final JJHttpRequest httpRequest) {
		return true;
	}

	@Override
	public RequestProcessor makeRequestProcessor(
		final JJHttpRequest request,
		final JJHttpResponse response
	) throws IOException {
		final URIMatch match = new URIMatch(request.uri());
		final StaticResource sr = resourceFinder.loadResource(StaticResource.class, match.baseName);
		if (sr != null && isServablePath(sr.path())) {
			return new RequestProcessor() {
				
				@Override
				public void process() {

					try {
						
						if (request.hasHeader(HttpHeaders.Names.IF_NONE_MATCH) &&
							sr.sha1().equals(request.header(HttpHeaders.Names.IF_NONE_MATCH))) {
							
							response.sendNotModified(sr);
	
						} else if (match.sha == null) {
							
							response.sendUncachedResource(sr);
							
						} else if (!match.sha.equals(sr.sha1())) {
						
							response.sendTemporaryRedirect(sr);
							
						} else {
							
							response.sendCachedResource(sr);
						}
						
					} catch (Throwable e) {
						response.error(e);
					}
				}
			};
		}
		
		return null;
		
	}

}
