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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class ApplicationTest {
	
	private final URL clamwhoresURL = ApplicationTest.class.getResource("");

	private Application clamwhores() throws Exception {
		return new Application(clamwhoresURL);
	}
	
	@Test @Ignore
	public void testLifecycle() throws Exception {
		Application clamwhores = clamwhores();
		assertThat(clamwhores, is(notNullValue()));
		assertTrue("clamwhores did not indicate a successful load", clamwhores.loaded());
		assertFalse("clamwhores indicated closed when should be not closed", clamwhores.closed());
		clamwhores.close();
		assertTrue("clamwhores indicated not closed when should be closed", clamwhores.closed());
	}
	
	@Test @Ignore
	public void testApplicationRespondsToExpectedURLs() throws Exception {
		Application clamwhores = clamwhores();
		assertTrue("clamwhores should respond to /index", clamwhores.respond("/index"));
		assertTrue("clamwhores should respond to /style.css", clamwhores.respond("/style.css"));
		assertTrue("clamwhores should respond to /clamwhores.com.png", clamwhores.respond("/clamwhores.com.png"));
		assertTrue("clamwhores should respond to /fragment", clamwhores.respond("/fragment"));
		assertFalse("clamwhores should not respond to /random", clamwhores.respond("/random"));
		assertFalse("clamwhores should not respond to /paths", clamwhores.respond("/paths"));
		assertFalse("clamwhores should not respond to /do", clamwhores.respond("/do"));
		assertFalse("clamwhores should not respond to /notwork", clamwhores.respond("/notwork"));
	}
}
