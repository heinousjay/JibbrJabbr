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

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Stage;

/**
 * @author jason
 *
 */
public class CoreModuleTest {

	@Test
	public void testRunningBuild() {
		
		// this should be enough to test that the core module builds
		Guice.createInjector(Stage.PRODUCTION, new CoreModule(new String[0], false)).getInstance(JJServerLifecycle.class);
	}
	
	@Test
	public void testTestBuild() {

		Guice.createInjector(Stage.PRODUCTION, new CoreModule(new String[0], true)).getInstance(JJServerLifecycle.class);
	}
}
