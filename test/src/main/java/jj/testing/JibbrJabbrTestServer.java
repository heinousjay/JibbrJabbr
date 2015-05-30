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

import static jj.testing.HttpTraceMode.*;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.JJModule;
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
 * There are some fluent configuration methods
 * 
 * 
 * @author jason
 *
 */
public class JibbrJabbrTestServer implements TestRule {
	
	static {
		ResourceLeakDetector.setLevel(Level.PARANOID);
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
		// need to create a test logger that searches the log output for leaks
		// and fail the test and kill the world
	}
	
	private HttpTraceMode mode = Nothing;
	
	private final Path rootPath;
	
	private final Path appPath;
	
	private boolean fileWatcher = false;
	
	private boolean httpServer = false;
	
	private int httpPort = 0;
	
	private boolean runAllSpecs = false;
	
	private Object instance;
	
	ArrayList<JJModule> modules;
	
	private Injector injector;
	
	/**
	 * Construct a test server, passing the appPath as
	 * the app parameter.
	 * @param appPath
	 */
	public JibbrJabbrTestServer(final Path rootPath, final Path appPath) {
		this.rootPath = rootPath;
		this.appPath = appPath;
	}
	
	public JibbrJabbrTestServer verifying() {
		assertNotStarted();
		
		mode = Verifying;
		return this;
	}
	
	public JibbrJabbrTestServer recording() {
		assertNotStarted();
		
		mode = Recording;
		return this;
	}
	
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
	
	public JibbrJabbrTestServer runAllSpecs() {
		assertNotStarted();
		
		runAllSpecs = true;
		return this;
	}
	
	public JibbrJabbrTestServer injectInstance(Object instance) {
		assertNotStarted();
		
		this.instance = instance;
		return this;
	}
	
	public JibbrJabbrTestServer withModule(JJModule module) {
		assertNotStarted();
		
		if (modules == null) {
			modules = new ArrayList<>(1); // probably
		}
		modules.add(module);
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
			result.baseUrl("http://0.0.0.0:" + httpPort);
		}
		
		return result;
	}
	
	public int httpPort() {
		return httpPort;
	}
	
	private void assertNotStarted() {
		assert injector == null : "server must be configured outside of runs!";
	}
	
	/**
	 * statement that injects a test instance
	 * @author jason
	 *
	 */
	@Singleton
	private static class TestInjectionStatement extends JibbrJabbrTestStatement {
		
		private final Injector injector;
		private final JibbrJabbrTestServer serverRule;

		@Inject
		TestInjectionStatement(final Injector injector, final JibbrJabbrTestServer serverRule) {
			this.injector = injector;
			this.serverRule = serverRule;
		}
		
		@Override
		public void evaluate() throws Throwable {
			injector.injectMembers(serverRule.instance);
			evaluateInner();
		}
	}
	
	/**
	 * statement that manages the test injector, and sets its children up
	 * @author jason
	 *
	 */
	private class InjectorManagerStatement extends JibbrJabbrTestStatement {
		
		InjectorManagerStatement(TestMethodStatement baseStatement) {
			ServerLifecycleStatement statement = injector.getInstance(ServerLifecycleStatement.class);
			statement.inner(baseStatement);
			if (httpServer) {
				statement.inner(injector.getInstance(HttpServerStatement.class));
			}
			inner(statement);
		}
		
		@Override
		public void evaluate() throws Throwable {
			try {
				injector.injectMembers(JibbrJabbrTestServer.this);
				evaluateInner();
			} finally {
				injector = null;
			}
		}
	}
	
	/**
	 * innermost statement that executes the test method.  this is just a debugging wrapper
	 * @author jason
	 *
	 */
	private static class TestMethodStatement extends JibbrJabbrTestStatement {

		private final Statement base;
		
		TestMethodStatement(Statement base) {
			this.base = base;
		}
		
		@Override
		public void evaluate() throws Throwable {
			base.evaluate();
		}
		
	}
	
	@Override
	public Statement apply(final Statement base, final Description description) {
		
		ArrayList<String> argBuilder = new ArrayList<>();
		argBuilder.add("server-root=" + rootPath);
		argBuilder.add("app=" + appPath);
		argBuilder.add("fileWatcher=" + fileWatcher);
		argBuilder.add("httpServer=" + httpServer);
		argBuilder.add("runAllSpecs=" + runAllSpecs);
		argBuilder.add("http-trace-mode=" + mode);
		if (httpPort > 1023 && httpPort < 65536) {
			argBuilder.add("httpPort=" + httpPort);
		}
		injector = Guice.createInjector(
			Stage.PRODUCTION,
			new TestModule(this, argBuilder.toArray(new String[argBuilder.size()]), description, httpServer)
		);
		
		JibbrJabbrTestStatement statement = new InjectorManagerStatement(new TestMethodStatement(base));
		if (instance != null) {
			statement.inner(injector.getInstance(TestInjectionStatement.class));
		}
		statement = mode.traceStatement(statement, description.getClassName() + "." + description.getMethodName());
		return statement;
	}

	/**
	 * @return The base URL of the HTTP server
	 */
	public String baseUrl() {
		return "http://localhost:" + (httpPort > 1023 && httpPort < 65536 ? httpPort : 8080);
	}
}
