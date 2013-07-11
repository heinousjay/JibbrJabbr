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
package jj.jjmessage;

import static jj.jjmessage.JJMessage.Type.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Map;

import jj.jjmessage.JJMessage;

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

	private void verifyExpectsNoResult(JJMessage message) {
		assertThat(message.expectsResult(), is(false));
		assertThat(message.id(), is(nullValue()));
		assertThat(message.resultId(), is(nullValue()));
	}
	
	private void verifyExpectsResult(JJMessage message) {
		assertThat(message.expectsResult(), is(true));
		assertThat(message.id(), is(notNullValue()));
	}

	@Test
	public void testAppend() throws Exception {
		JJMessage message = JJMessage.makeAppend("parent", "child");
		assertThat(message, is(notNullValue()));
		assertThat(message.type(), is(Append));
		assertThat(message.append(), is(notNullValue()));
		
		verifyExpectsNoResult(message);
		
		assertThat(message.append().parent, is("parent"));
		assertThat(message.append().child, is("child"));
		
		
		boolean errored = false;
		
		try {
			JJMessage.makeAppend(null, "child");
		} catch (AssertionError e) { errored = true; }
		
		if (!errored) fail("append should not have allowed null parent");
		
		try {
			JJMessage.makeAppend("", "child");
		} catch (AssertionError e) { errored = true; }
		
		if (!errored) fail("append should not have allowed empty parent");
		
		try {
			JJMessage.makeAppend("parent", null);
		} catch (AssertionError e) { errored = true; }
		
		if (!errored) fail("append should not have allowed null child");
		
		try {
			JJMessage.makeAppend("parent", "");
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
		JJMessage message = JJMessage.makeBind("context", "selector", "type");
		assertThat(message, is(notNullValue()));
		assertThat(message.type(), is(Bind));
		assertThat(message.bind(), is(notNullValue()));
		
		verifyExpectsNoResult(message);
		
		assertThat(message.bind().context, is("context"));
		assertThat(message.bind().selector, is("selector"));
		assertThat(message.bind().type, is("type"));
		
		boolean errored = false;
		
		try {
			JJMessage.makeBind(null, "selector", null);
		} catch (AssertionError e) { errored = true; }
		
		if (!errored) fail("bind should not have allowed null type");
		
		try {
			JJMessage.makeBind(null, "selector", "");
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
	
	@Test
	public void testCall() throws Exception {
		JJMessage message = JJMessage.makeCall("name", "[]");
		assertThat(message, is(notNullValue()));
		assertThat(message.type(), is(Call));
		assertThat(message.call(), is(notNullValue()));
		
		verifyExpectsNoResult(message);
		
		assertThat(message.call().name, is("name"));
		assertThat(message.call().args, is("[]"));
		
		boolean errored = false;
		
		try {
			JJMessage.makeCall(null, "[]");
		} catch (AssertionError e) { errored = true; }
		
		if (!errored) fail("call should not have allowed null name");
		
		try {
			JJMessage.makeCall("name", "");
		} catch (AssertionError e) { errored = true; }
		
		if (!errored) fail("call should not have allowed empty type");
		
		String serialized = message.toString();
		
		Map<String, Map<String, String>> map = 
			mapper.readValue(serialized, new TypeReference<Map<String, Map<String, String>>>() {});
		
		assertThat(map.containsKey("call"), is(true));
		assertThat(map.size(), is(1));
		Map<String, String> map2 = map.get("call");
		assertThat(map2.get("name"), is("name"));
		assertThat(map2.get("args"), is("[]"));
		assertThat(map2.size(), is(2));
	}
	
	@Test
	public void testInvoke() throws Exception {
		JJMessage message = JJMessage.makeInvoke("name", "[]");
		assertThat(message, is(notNullValue()));
		assertThat(message.type(), is(Invoke));
		assertThat(message.invoke(), is(notNullValue()));
		
		verifyExpectsResult(message);
		
		assertThat(message.invoke().name, is("name"));
		assertThat(message.invoke().args, is("[]"));
		
		boolean errored = false;
		
		try {
			JJMessage.makeInvoke(null, "[]");
		} catch (AssertionError e) { errored = true; }
		
		if (!errored) fail("invoke should not have allowed null name");
		
		try {
			JJMessage.makeInvoke("name", "");
		} catch (AssertionError e) { errored = true; }
		
		if (!errored) fail("invoke should not have allowed empty type");
		
		String serialized = message.toString();
		
		Map<String, Map<String, String>> map = 
			mapper.readValue(serialized, new TypeReference<Map<String, Map<String, String>>>() {});
		
		assertThat(map.containsKey("invoke"), is(true));
		assertThat(map.size(), is(1));
		Map<String, String> map2 = map.get("invoke");
		assertThat(map2.get("name"), is("name"));
		assertThat(map2.get("args"), is("[]"));
		assertThat(map2.get("id"), is(notNullValue()));
		assertThat(map2.size(), is(3));
	}
}
