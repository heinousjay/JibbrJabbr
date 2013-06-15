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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import jj.configuration.Configuration;

import org.junit.Before;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class ConfigurationTest {
	
	URI realUri = URI.create("http://localhost:8080/");
	Path realPath;

	@Before 
	public void before() throws Exception {
		realPath = Paths.get(ConfigurationTest.class.getResource("/index.html").toURI()).getParent();
	}
	
	@Test
	public void test() {
		Configuration toTest = new Configuration(new String[] {realUri.toString(), realPath.toString()});
		
		assertThat(toTest.basePath(), is(realPath));
		assertThat(toTest.baseUri(), is(realUri));
		
		toTest = new Configuration(new String[] {realPath.toString(), realUri.toString()});
		
		assertThat(toTest.basePath(), is(realPath));
		assertThat(toTest.baseUri(), is(realUri));
	}

}
