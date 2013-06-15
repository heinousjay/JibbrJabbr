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

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.JJExecutors;
import jj.JJRunnable;
import jj.configuration.Configuration;
import jj.resource.ResourceFinder;
import jj.resource.StaticResource;
import jj.webbit.JJHttpRequest;
import jj.webbit.RequestProcessor;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpResponse;

/**
 * @author jason
 *
 */
@Singleton
public class StaticServable extends Servable {

	private final ResourceFinder resourceFinder;
	
	private final JJExecutors executors;
	
	/**
	 * @param configuration
	 */
	@Inject
	protected StaticServable(
		final Configuration configuration,
		final ResourceFinder resourceFinder,
		final JJExecutors executors
	) {
		super(configuration);
		this.resourceFinder = resourceFinder;
		this.executors = executors;
	}
	
	/**
	 * we always need IO
	 */
	@Override
	public boolean needsIO(final JJHttpRequest request) {
		return true;
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
		final HttpResponse response,
		final HttpControl control
	) throws IOException {
		
		final StaticResource sr = resourceFinder.loadResource(StaticResource.class, baseName(request));
		if (sr != null && isServablePath(sr.path())) {
			return new RequestProcessor() {
				
				@Override
				public void process() {
					executors.httpControlExecutor().submit(
						executors.prepareTask(new JJRunnable("static resource http serving") {
						
							@Override
							public void run() throws Exception {
								response
									.header(HttpHeaders.Names.CONTENT_TYPE, sr.mime())
									.content(sr.bytes())
									.end();
							}
						})
					);
				}
			};
		}
		
		return null;
		
	}

}
