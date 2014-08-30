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
package jj.http.uri;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class RouteUriValidatorTest {
	
	RouteUriValidator ruv = new RouteUriValidator();
	
	@Test
	public void testValidUris() {

		assertThat(ruv.validateRouteUri("/"), is(""));
		assertThat(ruv.validateRouteUri("/static"), is(""));
		assertThat(ruv.validateRouteUri("/static/*path"), is(""));
		assertThat(ruv.validateRouteUri("/static/:param/*path"), is(""));
		

		assertThat(ruv.validateRouteUri("/this/is"), is(""));
		assertThat(ruv.validateRouteUri("/this/isno"), is(""));
		assertThat(ruv.validateRouteUri("/this/isnot"), is(""));
		assertThat(ruv.validateRouteUri("/this/is/the/bomb"), is(""));
		assertThat(ruv.validateRouteUri("/this/is/the/bomberman"), is(""));
		assertThat(ruv.validateRouteUri("/this/is/the/best"), is(""));
		assertThat(ruv.validateRouteUri("/this/is/the/best-around"), is(""));
		assertThat(ruv.validateRouteUri("/this/:is/:the/best"), is(""));
		assertThat(ruv.validateRouteUri("/this/:is/:the/*end"), is(""));
		assertThat(ruv.validateRouteUri("/this/*islast_and_should_not_interfere"), is(""));
		assertThat(ruv.validateRouteUri("/this/*islast_and_also_is_not_used"), is(""));
		assertThat(ruv.validateRouteUri("/some.directory/*path.css"), is(""));
		assertThat(ruv.validateRouteUri("/some.directory/path.*css"), is(""));
		assertThat(ruv.validateRouteUri("/*path.css"), is(""));
		assertThat(ruv.validateRouteUri("/*path.:ext"), is(""));
		assertThat(ruv.validateRouteUri("/user/:id([a-z]-[\\d]{6})/picture"), is(""));
		assertThat(ruv.validateRouteUri("/user/:name([\\w]+)/picture.jpg"), is(""));
	}

	@Test
	public void testErrors() {
		assertThat(ruv.validateRouteUri(null), is("uri must not be null"));
		assertThat(ruv.validateRouteUri(""), is("uri must start with /"));
		assertThat(ruv.validateRouteUri("static"), is("uri must start with /"));
		
		assertThat(ruv.validateRouteUri("/*path/static"), is("* parameter must be the last path segment in a uri"));
		
		assertThat(ruv.validateRouteUri("/:1hahsk"), is("parameter :1hahsk must have a valid JavaScript variable name"));
		
		assertThat(ruv.validateRouteUri("/:a(\\l)"), is("parameter :a(\\l) pattern \\l failed to compile\nIllegal/unsupported escape sequence near index 1\n\\l\n ^"));
		
		assertThat(ruv.validateRouteUri("/*a(\\l)"), is("parameter *a(\\l) pattern \\l failed to compile\nIllegal/unsupported escape sequence near index 1\n\\l\n ^"));
		
		assertThat(ruv.validateRouteUri("/path.:a(\\l)"), is("parameter :a(\\l) pattern \\l failed to compile\nIllegal/unsupported escape sequence near index 1\n\\l\n ^"));
		
		assertThat(ruv.validateRouteUri("/path.*a(\\l)"), is("parameter *a(\\l) pattern \\l failed to compile\nIllegal/unsupported escape sequence near index 1\n\\l\n ^"));
		
		assertThat(ruv.validateRouteUri("/:b(\\l).:a(\\l)"), is(
			"parameter :b(\\l) pattern \\l failed to compile\nIllegal/unsupported escape sequence near index 1\n\\l\n ^\n" +
			"parameter :a(\\l) pattern \\l failed to compile\nIllegal/unsupported escape sequence near index 1\n\\l\n ^")
		);
		
		assertThat(ruv.validateRouteUri("/*b(\\l).:a(\\l)"), is(
			"parameter *b(\\l) pattern \\l failed to compile\nIllegal/unsupported escape sequence near index 1\n\\l\n ^\n" +
			"parameter :a(\\l) pattern \\l failed to compile\nIllegal/unsupported escape sequence near index 1\n\\l\n ^")
		);
	}

}
