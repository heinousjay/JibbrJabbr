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
package jj.configuration;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import jj.script.RealRhinoContextProvider;
import jj.script.RhinoContext;

import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author jason
 *
 */
public class SystemPropertiesScriptableTest {

	private static final String JAVA_HOME = "java.home";

	ScriptableObject global;
	
	SystemPropertiesScriptable sysProps;
	
	@Before
	public void before() {
		try (RhinoContext context = new RealRhinoContextProvider().get()) {
			global = context.initStandardObjects(true);
		}
		
		sysProps = new SystemPropertiesScriptable(global);
 	}
	
	@Test
	public void test() {
		
		assertTrue(sysProps.has(JAVA_HOME, sysProps)); // this is always there?
		assertThat(sysProps.get(JAVA_HOME, sysProps), is(System.getProperty(JAVA_HOME)));
	}

}
