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
package jj.css;

import static jj.configuration.resolution.AppLocation.Base;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import jj.resource.MockAbstractResourceDependencies;

import org.junit.Before;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class LessResourceTest {
	
	MockAbstractResourceDependencies dependencies;
	
	@Before
	public void before() {
		dependencies = new MockAbstractResourceDependencies(Base);
	}

	private LessResource lessResource(String name) throws Exception {
		
		Path path = Paths.get(LessResourceTest.class.getResource("/" + name).toURI());
		
		return new LessResource(dependencies, name, path);
	}
	
	@Test
	public void test() throws Exception {
		LessResource lr = lessResource("jj/resource/test.less");
		
		assertThat(lr.contents(), is(notNullValue()));
		// ehh good enough!
		assertThat(lr.contents().length(), is(153));
	}

}