package jj.engine;

import java.util.Collections;
import java.util.Map;

import jj.engine.EngineAPIImpl;
import jj.engine.HostObject;
import jj.engine.ScriptJSON;

import org.junit.Test;

public class ScriptJSONTest {
	
	private static final String TEST_STRING = "{\"form\":\"{\\\"userName\\\":\\\"jaybird\\\"}\"}";

	@Test
	public void test() {
		EngineAPIImpl rhinoObjectCreator = new EngineAPIImpl(Collections.<HostObject>emptySet());
		
		ScriptJSON underTest = new ScriptJSON(rhinoObjectCreator);
		
		Map<String, Object> map = (Map<String, Object>)underTest.parse(TEST_STRING);
		underTest.parse(String.valueOf(map.get("form")));
	}

}
