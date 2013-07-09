package jj.hostapi;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;

public class ScriptJSONTest {
	
	private static final String TEST_STRING = "{\"form\":\"{\\\"userName\\\":\\\"jaybird\\\"}\"}";

	@Test
	public void test() {
		RhinoObjectCreatorImpl rhinoObjectCreator = new RhinoObjectCreatorImpl(Collections.<HostObject>emptySet());
		
		ScriptJSON underTest = new ScriptJSON(rhinoObjectCreator);
		
		Map<String, Object> map = (Map<String, Object>)underTest.parse(TEST_STRING);
		underTest.parse(String.valueOf(map.get("form")));
	}

}