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
package jj.jqmessage;

import static jj.jqmessage.JQueryMessage.Type.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * validates the messaging API.  ugly test, ugly ugly test.
 * 
 * @author jason
 *
 */
public class MessagesTest {
	
	ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testAppend() throws Exception {
		JQueryMessage message = JQueryMessage.makeAppend("parent", "child");
		assertThat(message, is(notNullValue()));
		assertThat(message.type(), is(Append));
		
		assertThat(message.append(), is(notNullValue()));
		assertThat(message.bind(), is(nullValue()));
		assertThat(message.call(), is(nullValue()));
		assertThat(message.create(), is(nullValue()));
		assertThat(message.element(), is(nullValue()));
		assertThat(message.event(), is(nullValue()));
		assertThat(message.get(), is(nullValue()));
		assertThat(message.invoke(), is(nullValue()));
		assertThat(message.result(), is(nullValue()));
		assertThat(message.retrieve(), is(nullValue()));
		assertThat(message.set(), is(nullValue()));
		assertThat(message.store(), is(nullValue()));
		
		assertThat(message.expectsResult(), is(false));
		assertThat(message.id(), is(nullValue()));
		assertThat(message.resultId(), is(nullValue()));
		
		assertThat(message.append().parent, is("parent"));
		assertThat(message.append().child, is("child"));
		
		
		boolean errored = false;
		
		try {
			JQueryMessage.makeAppend(null, "child");
		} catch (AssertionError e) { errored = true; }
		
		if (!errored) fail("append should not have allowed null parent");
		
		try {
			JQueryMessage.makeAppend("", "child");
		} catch (AssertionError e) { errored = true; }
		
		if (!errored) fail("append should not have allowed empty parent");
		
		try {
			JQueryMessage.makeAppend("parent", null);
		} catch (AssertionError e) { errored = true; }
		
		if (!errored) fail("append should not have allowed null child");
		
		try {
			JQueryMessage.makeAppend("parent", "");
		} catch (AssertionError e) { errored = true; }
		
		if (!errored) fail("append should not have allowed empty child");
		
		String serialized = message.toString();
		
		Map<String, Map<String, String>> map = 
			mapper.readValue(serialized, new TypeReference<Map<String, Map<String, String>>>() {});
		
		assertThat(map.containsKey("append"), is(true));
		assertThat(map.size(), is(1));
		Map<String, String> map2 = map.get("append");
		assertThat(map2.get("parent"), is("parent"));
		assertThat(map2.get("child"), is("child"));
		assertThat(map2.size(), is(2));
	}

	@Test
	public void testBind() throws Exception {
		JQueryMessage message = JQueryMessage.makeBind("context", "selector", "type");
		assertThat(message, is(notNullValue()));
		assertThat(message.type(), is(Bind));
		
		assertThat(message.append(), is(nullValue()));
		assertThat(message.bind(), is(notNullValue()));
		assertThat(message.call(), is(nullValue()));
		assertThat(message.create(), is(nullValue()));
		assertThat(message.element(), is(nullValue()));
		assertThat(message.event(), is(nullValue()));
		assertThat(message.get(), is(nullValue()));
		assertThat(message.invoke(), is(nullValue()));
		assertThat(message.result(), is(nullValue()));
		assertThat(message.retrieve(), is(nullValue()));
		assertThat(message.set(), is(nullValue()));
		assertThat(message.store(), is(nullValue()));
		
		assertThat(message.expectsResult(), is(false));
		assertThat(message.id(), is(nullValue()));
		assertThat(message.resultId(), is(nullValue()));
		
		assertThat(message.bind().context, is("context"));
		assertThat(message.bind().selector, is("selector"));
		assertThat(message.bind().type, is("type"));
		
		boolean errored = false;
		
		try {
			JQueryMessage.makeBind(null, "selector", null);
		} catch (AssertionError e) { errored = true; }
		
		if (!errored) fail("bind should not have allowed null type");
		
		try {
			JQueryMessage.makeBind(null, "selector", "");
		} catch (AssertionError e) { errored = true; }
		
		if (!errored) fail("bind should not have allowed empty type");
		
		String serialized = message.toString();
		
		Map<String, Map<String, String>> map = 
			mapper.readValue(serialized, new TypeReference<Map<String, Map<String, String>>>() {});
		
		assertThat(map.containsKey("bind"), is(true));
		assertThat(map.size(), is(1));
		Map<String, String> map2 = map.get("bind");
		assertThat(map2.get("context"), is("context"));
		assertThat(map2.get("selector"), is("selector"));
		assertThat(map2.get("type"), is("type"));
		assertThat(map2.size(), is(3));
	}
}
