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
package jj.testing;

import java.net.URI;
import java.util.ArrayList;

import jj.webdriver.WebDriverProvider;
import jj.webdriver.WebDriverRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

/**
 * <p>
 * Test rule to encapsulate a JibbrJabbr server.
 * 
 * <p>
 * The server is stood up immediately before each test
 * method, and torn down immediately after.
 * 
 * <p>
 * 
 * 
 * 
 * @author jason
 *
 */
public class JibbrJabbrTestServer implements TestRule {
	
	private final String appPath;
	
	private boolean fileWatcher = false;
	
	private boolean httpServer = false;
	
	private int httpPort = 0;
	
	private Object instance;
	
	private Injector injector;
	
	/**
	 * Construct a test server, passing the appPath as
	 * the app parameter.
	 * @param appPath
	 */
	public JibbrJabbrTestServer(final String appPath) {
		this.appPath = appPath;
	}
	
	/**
	 * 
	 */
	public JibbrJabbrTestServer(final URI appURI) {
		assert "file".equals(appURI.getScheme()) : "";
		this.appPath = appURI.getPath();
	}
	
	/**
	 * 
	 * @return
	 */
	public JibbrJabbrTestServer withFileWatcher() {
		assertNotStarted();
		
		fileWatcher = true;
		return this;
	}
	
	public JibbrJabbrTestServer withHttp() {
		assertNotStarted();
		
		httpServer = true;
		
		return this;
	}
	
	public JibbrJabbrTestServer withHttpOnPort(int port) {
		assert (port > 1023 && port < 65536) : "http port must be between 1024-65535 inclusive";
		assertNotStarted();
		
		httpServer = true;
		httpPort = port;
		return this;
	}
	
	public JibbrJabbrTestServer injectInstance(Object instance) {
		assertNotStarted();
		
		this.instance = instance;
		return this;
	}
	
	public WebDriverRule webDriverRule(Class<? extends WebDriverProvider> webDriverProvider) {
		assertNotStarted();
		if (!httpServer) {
			// todo - pick a local open port
			withHttpOnPort(8080);
		}
		
		WebDriverRule result = 
			new WebDriverRule().driverProvider(webDriverProvider);
		
		if (httpPort != 0) {
			result.baseUrl("http://localhost:" + httpPort);
		}
		
		return result;
	}
	
	public int httpPort() {
		return httpPort;
	}
	
	private Statement createInjectionStatement(final Statement base) {
		return new Statement() {
			
			@Override
			public void evaluate() throws Throwable {
				if (instance != null) {
					injector.injectMembers(instance);
				}
				base.evaluate();
				
			}
		};
	}
	
	private void assertNotStarted() {
		assert injector == null : "server must be configured outside of runs!";
	}
	
	@Override
	public Statement apply(final Statement base, final Description description) {
		
		ArrayList<String> builder = new ArrayList<>();
		builder.add("app=" + appPath);
		builder.add("fileWatcher=" + fileWatcher);
		builder.add("httpServer=" + httpServer);
		if (httpPort > 1023 && httpPort < 65536) {
			builder.add("httpPort=" + httpPort);
		}
		
		injector = Guice.createInjector(
			Stage.PRODUCTION,
			new TestModule(this, builder.toArray(new String[builder.size()]), base, description, httpServer)
		);
		
		Statement statement = new Statement() {
			@Override
			public void evaluate() throws Throwable {
				try {
					injector.getInstance(AppStatement.class).evaluate();
				} finally {
					injector = null;
				}
			}
		};
		if (instance != null) {
			statement = createInjectionStatement(statement);
		}
		
		return statement;
	}
}
