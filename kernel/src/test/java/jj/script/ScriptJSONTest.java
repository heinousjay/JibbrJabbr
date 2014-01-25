package jj.script;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import jj.script.ScriptJSON;

import org.junit.Test;

public class ScriptJSONTest {
	
	private static final String TEST_STRING = "{\"form\":\"{\\\"userName\\\":\\\"jaybird\\\",\\\"userLame\\\":\\\"sure plus a \\\\\\\\slash for testing\\\"}\"}";
	
	@SuppressWarnings("unchecked")
	@Test
	public void test() throws Exception {
		
		ScriptJSON underTest = new ScriptJSON(new RealRhinoContextProvider());
		
		Map<String, String> map = (Map<String, String>)underTest.parse(TEST_STRING);
		String form = map.get("form");
		
		map = (Map<String, String>)underTest.parse(form);
		
		assertThat(map.get("userName"), is("jaybird"));
		assertThat(map.get("userLame"), is("sure plus a \\slash for testing"));
	}

}
