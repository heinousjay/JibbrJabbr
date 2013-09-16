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
package jj.resource;

import static org.mockito.BDDMockito.*;

import org.slf4j.Logger;

import jj.configuration.Configuration;
import jj.resource.html.HtmlResource;
import jj.resource.html.HtmlResourceMaker;
import jj.resource.stat.ic.StaticResource;
import jj.resource.stat.ic.StaticResourceMaker;

/**
 * @author jason
 *
 */
public class ResourceMaker {
	
	private final Logger logger;
	private final Configuration configuration;
	private final ResourceInstanceCreator creator;
	
	public ResourceMaker(Configuration configuration) throws Exception {
		this.configuration = configuration;
		logger = mock(Logger.class);
		creator = ResourceInstanceCreatorTest.creator(configuration, logger);
	}
	
	public ResourceMaker(Configuration configuration, Logger logger) throws Exception {
		this.configuration = configuration;
		this.logger = logger;
		creator = ResourceInstanceCreatorTest.creator(configuration, logger);
	}

	public StaticResource makeStatic(String baseName) throws Exception {
		return StaticResourceMaker.make(configuration, creator, baseName);
	}
	
	public HtmlResource makeHtml(String baseName) throws Exception {
		return HtmlResourceMaker.make(configuration, creator, baseName);
	}
}
