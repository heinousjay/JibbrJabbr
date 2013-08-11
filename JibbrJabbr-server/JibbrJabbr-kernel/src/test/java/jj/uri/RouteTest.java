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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class RouteTest {
	
	@Test
	public void testInputValidation() {
		try {
			new Route(null);
			fail("should have thrown");
		} catch (IllegalArgumentException iae) {}
		try {
			new Route("");
			fail("should have thrown");
		} catch (IllegalArgumentException iae) {}
		try {
			new Route("chat/{room=lobby}/");
			fail("should have thrown");
		} catch (IllegalArgumentException iae) {}
	}

	@Test
	public void test() {
		Route chat = new Route("/chat/{room}/");
		assertThat(chat.matches("/chat/room/"), is(true));
		assertThat(chat.matches("/chat/room"), is(false));
		
		Map<String, String> params = chat.readParams("/chat/room2/");
		assertThat(params.get("room"), is("room2"));
		assertThat(params.size(), is(1));
		
		String uri = chat.generate(params);
		assertThat(uri, is("/chat/room2/"));
		params.put("room", "new-room");
		uri = chat.generate(params);
		assertThat(uri, is("/chat/new-room/"));
		
	}

}
