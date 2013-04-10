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
import jj.JJServerLifecycle;
import jj.StringUtils;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jsoup.nodes.Document;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

/**
 * A test rule that supplies a context for performing
 * tests against a given HTML resource.
 * 
 * @author jason
 *
 */
public class JJAppTest implements TestRule {
	
	private final String basePath;
	
	private Injector injector;
	
	public JJAppTest(final String basePath) {
		this.basePath = basePath;
	}
	
	@Override
	public Statement apply(final Statement base, final Description description) {
		
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				
				// we use production to eagerly instantiate the graph, since the next line will do
				// that anyway.  might be adding the concept of modules back, simple rules to start -
				// some derivative of Module in your base package (first directory in the jar that has
				// classes, and that's about it.  you can inject anything public.  the classloader will
				// treat jj. just like java. in that it will be off limits?  is this smart? it's
				// probably better to start restrictive and loosen if needed.  ModuleListener interface
				// to get a call on start-up
				injector = Guice.createInjector(Stage.PRODUCTION, new TestModule(basePath, description));
				
				try {
					injector.getInstance(JJServerLifecycle.class).start();
					base.evaluate();
				} finally {
					injector.getInstance(JJServerLifecycle.class).stop();
					injector = null;
				}
			}
		};
	}
	
	public TestClient doSocketConnection(final Document document) throws Exception {
		String url = document.select("script[data-jj-socket-url]").attr("data-jj-socket-url");
		assertThat("document doesn't have socket script", StringUtils.isEmpty(url), is(false));
		TestClient client = get(url);
		client.dumpObjects();
		return client;
	}
	
	public TestClient get(final String uri) throws Exception {
		assertThat("supply a uri pls", uri, is(notNullValue()));
		TestRunner runner = injector.getInstance(TestRunner.class);
		
		runner.request().uri(uri)
			.timestamp(System.nanoTime())
			.header(HttpHeaders.Names.HOST, "localhost");
		
		return runner.run();
	}
}
