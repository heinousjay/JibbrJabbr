package jj.engine;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import jj.engine.ScriptJSON;
import jj.script.RealRhinoContextMaker;

import org.junit.Test;

public class ScriptJSONTest {
	
	private static final String TEST_STRING = "{\"form\":\"{\\\"userName\\\":\\\"jaybird\\\"}\"}";

	@SuppressWarnings("unchecked")
	@Test
	public void test() {
		
		ScriptJSON underTest = new ScriptJSON(new RealRhinoContextMaker());
		
		Map<String, Object> map = (Map<String, Object>)underTest.parse(TEST_STRING);
		map = (Map<String, Object>)underTest.parse(String.valueOf(map.get("form")));
		
		assertThat((String)map.get("userName"), is("jaybird"));
	}

}
