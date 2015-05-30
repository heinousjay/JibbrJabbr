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
	
	public static final Path one;
	
	public static final Path two;
	
	public static final Path minimal;
	
	public static final Path configuration;
	
	public static final Path css;
	
	public static final Path httpClient;

	public static final Path repl;

	public static final Path module;

	public static final Path jasmine;
	
	static {
		try {
			one = getPath("/app1/app/");
			two = getPath("/app2/app/");
			minimal = getPath("/minimal/app/");
			configuration = getPath("/configuration/");
			css = getPath("/css/");
			httpClient = getPath("/http/client/");
			repl = getPath("/repl/");
			module = getPath("/module/");
			jasmine = getPath("/jasmine/");
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	private static Path getPath(String p) throws URISyntaxException {
		return Paths.get(App.class.getResource(p).toURI()).toAbsolutePath();
	}
}
