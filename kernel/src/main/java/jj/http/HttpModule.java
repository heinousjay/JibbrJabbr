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
package jj.http;

import jj.JJModule;
import jj.http.client.HttpClientModule;
import jj.http.server.HttpServerModule;
import jj.http.server.uri.URIModule;

/**
 * @author jason
 *
 */
public class HttpModule extends JJModule {

	@Override
	protected void configure() {
		install(new HttpClientModule());
		install(new HttpServerModule());
		install(new URIModule());
	}

}
