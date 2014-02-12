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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.Field;

import javax.inject.Inject;

import jj.StringUtils;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

import org.jsoup.nodes.Document;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

/**
 * A test rule that supplies a context for performing
 * tests against a given HTML resource.  This rule does
 * not support parallel execution of test methods
 * 
 * @author jason
 *
 */
public class JibbrJabbrTestServer implements TestRule {
	
	private final String appPath;
	
	private boolean fileWatcher = false;
	
	private boolean httpServer = false;
	
	private Object instance;
	
	private Injector injector;
	
	/**
	 * construct a test server with no file watching pointing to appPath
	 * 
	 * should that be a path? investigate
	 * @param appPath
	 */
	public JibbrJabbrTestServer(final String appPath) {
		this.appPath = appPath;
	}
	
	public JibbrJabbrTestServer withFileWatcher() {
		fileWatcher = true;
		return this;
	}
	
	public JibbrJabbrTestServer withHttpServer() {
		httpServer = true;
		return this;
	}
	
	public JibbrJabbrTestServer injectInstance(Object instance) {
		this.instance = instance;
		return this;
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
	
	@Override
	public Statement apply(final Statement base, final Description description) {
		
		// we use production to eagerly instantiate the graph, since the next line will do
		// that anyway.
		Statement statement = base;
		if (instance != null) {
			statement = createInjectionStatement(base);
		}
		
		injector = Guice.createInjector(Stage.PRODUCTION, new TestModule(this, appPath, statement, description));
		return injector.getInstance(AppStatement.class);
	}
	
	public TestHttpClient doSocketConnection(final Document document) throws Exception {
		String url = document.select("script[data-jj-socket-url]").attr("data-jj-socket-url");
		assertThat("document must have a socket script to use this method", StringUtils.isEmpty(url), is(false));
		TestHttpClient client = get(url);
		client.dumpObjects();
		return client;
	}
	
	public TestHttpClient get(final String uri) throws Exception {
		assertThat("supply a uri please", uri, is(notNullValue()));
		
		TestRunner runner = injector.createChildInjector(
			new RequestParameterModule(HttpMethod.GET, uri)
		).getInstance(TestRunner.class);
		
		runner.request().header(HttpHeaders.Names.HOST, "localhost");
		
		return runner.run();
	}
}
