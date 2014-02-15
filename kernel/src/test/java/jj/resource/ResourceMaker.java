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

import jj.configuration.AppLocation;
import jj.configuration.Application;
import jj.configuration.Configuration;
import jj.resource.document.HtmlResource;
import jj.resource.document.HtmlResourceMaker;
import jj.resource.script.ScriptResource;
import jj.resource.script.ScriptResourceMaker;
import jj.resource.stat.ic.StaticResource;
import jj.resource.stat.ic.StaticResourceMaker;

/**
 * @author jason
 *
 */
public class ResourceMaker {
	
	private final Logger logger;
	private final Application app;
	private final ResourceInstanceCreator creator;
	
	public ResourceMaker(final Configuration configuration, final Application app) throws Exception {
		this.app = app;
		logger = mock(Logger.class);
		creator = ResourceInstanceCreatorTest.creator(app, configuration, logger);
	}
	
	public ResourceMaker(Configuration configuration, final Application app, Logger logger) throws Exception {
		this.app = app;
		this.logger = logger;
		creator = ResourceInstanceCreatorTest.creator(app, configuration, logger);
	}

	public StaticResource makeStatic(AppLocation base, String name) throws Exception {
		return StaticResourceMaker.make(app, creator, base, name);
	}
	
	public HtmlResource makeHtml(AppLocation base, String name) throws Exception {
		return HtmlResourceMaker.make(app, creator, base, name);
	}
	
	public ScriptResource makeScript(AppLocation base, String name) throws Exception {
		return ScriptResourceMaker.make(app, creator, base, name);
	}
}
