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
package jj;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import jj.webdriver.WebDriverProvider;
import jj.webdriver.provider.PhantomJSWebDriverProvider;

/**
 * @author jason
 *
 */
public class App {
	
	// this could also be a helper class that inspects system properties or vm args or whatever
	public static final Class<? extends WebDriverProvider> DRIVER_PROVIDER = PhantomJSWebDriverProvider.class; //FirefoxWebDriverProvider.class;
	
	public static final Path app1;
	
	public static final Path app2;
	
	public static final Path configuration;

	public static final Path configuration2;
	
	public static final Path css;
	
	public static final Path httpClient;

	public static final Path jasmine;
	
	public static final Path minimal;

	public static final Path module;

	public static final Path repl;
	
	static {
		try {
			app1 = getPath("/test-apps/app1/");
			app2 = getPath("/test-apps/app2/");
			configuration = getPath("/test-apps/configuration/");
			configuration2 = getPath("/test-apps/configuration2/");
			css = getPath("/test-apps/css/");
			httpClient = getPath("/test-apps/http-client/");
			jasmine = getPath("/test-apps/jasmine/");
			minimal = getPath("/test-apps/minimal/");
			module = getPath("/test-apps/module/");
			repl = getPath("/test-apps/repl/");
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	private static Path getPath(String p) throws URISyntaxException {
		return Paths.get(App.class.getResource(p).toURI()).toAbsolutePath();
	}
}
