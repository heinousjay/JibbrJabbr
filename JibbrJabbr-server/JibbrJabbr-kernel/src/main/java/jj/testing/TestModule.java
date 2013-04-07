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

import org.junit.runner.Description;

import jj.CoreModule;
import jj.JJModule;

/**
 * @author jason
 *
 */
class TestModule extends JJModule {
	
	private final String basePath;
	private final Description description;
	
	TestModule(final String basePath, final Description description) {
		this.basePath = basePath;
		this.description = description;
	}

	@Override
	protected void configure() {
		
		addServerListenerBinding().to(TestListener.class);
		
		bind(Description.class).toInstance(description);
		
		install(new CoreModule(new String[]{basePath}, true));
	}
}
