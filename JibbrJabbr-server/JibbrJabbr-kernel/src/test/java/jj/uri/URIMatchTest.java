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
package jj.uri;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import jj.uri.URIMatch;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class URIMatchTest {
	
	@Test
	public void testVersioned() {
		URIMatch match = new URIMatch("/be03b9352e1e254cae9a58cff2b20e0c8d513e47/jj.js");
		assertThat(match.versioned, is(true));
		
		match = new URIMatch("/jquery-2.0.2.min.js");
		assertThat(match.versioned, is(true));
		
		match = new URIMatch("/jquery-2.0.2.js");
		assertThat(match.versioned, is(true));
	}

	@Test
	public void test() {
		URIMatch match = new URIMatch("/be03b9352e1e254cae9a58cff2b20e0c8d513e47/jj.js");
		assertThat(match.sha, is("be03b9352e1e254cae9a58cff2b20e0c8d513e47"));
		assertThat(match.baseName, is("jj.js"));
		assertThat(match.name, is("jj"));
		assertThat(match.extension, is("js"));
		assertThat(match.versioned, is(true));
	}
	
	@Test
	public void test2() {
		URIMatch match = new URIMatch("/jj.js");
		assertThat(match.sha, is(nullValue()));
		assertThat(match.baseName, is("jj.js"));
		assertThat(match.name, is("jj"));
		assertThat(match.extension, is("js"));
		assertThat(match.versioned, is(false));
	}

	@Test
	public void test3() {
		URIMatch match = new URIMatch("/be03b9352e1e254cae9a58cff2b20e0c8d547/jj.js");
		assertThat(match.sha, is(nullValue()));
		assertThat(match.baseName, is("be03b9352e1e254cae9a58cff2b20e0c8d547/jj.js"));
		assertThat(match.name, is("be03b9352e1e254cae9a58cff2b20e0c8d547/jj"));
		assertThat(match.extension, is("js"));
		assertThat(match.versioned, is(false));
	}

	@Test
	public void test4() {
		URIMatch match = new URIMatch("/be03b9352e1e254cae9a58cff2b20e0c8d513e47/be03b9352e1e254cae9a58cff2b20e0c8d513e47/jj.js");
		assertThat(match.sha, is("be03b9352e1e254cae9a58cff2b20e0c8d513e47"));
		assertThat(match.baseName, is("be03b9352e1e254cae9a58cff2b20e0c8d513e47/jj.js"));
		assertThat(match.name, is("be03b9352e1e254cae9a58cff2b20e0c8d513e47/jj"));
		assertThat(match.extension, is("js"));
		assertThat(match.versioned, is(true));
	}

	@Test
	public void test5() {
		URIMatch match = new URIMatch("/be03b9352e1e254cae9a58cff2b20e0c8d513e47/be03b9352e1e254cae9a58cff2b20e0c8d513e47/jj");
		assertThat(match.sha, is("be03b9352e1e254cae9a58cff2b20e0c8d513e47"));
		assertThat(match.baseName, is("be03b9352e1e254cae9a58cff2b20e0c8d513e47/jj"));
		assertThat(match.name, is("be03b9352e1e254cae9a58cff2b20e0c8d513e47/jj"));
		assertThat(match.extension, is(nullValue()));
		assertThat(match.versioned, is(true));
	}

	@Test
	public void test6() {
		URIMatch match = new URIMatch("/jquery.fancybox.css");
		assertThat(match.sha, is(nullValue()));
		assertThat(match.baseName, is("jquery.fancybox.css"));
		assertThat(match.name, is("jquery.fancybox"));
		assertThat(match.extension, is("css"));
		assertThat(match.versioned, is(false));
	}
}
