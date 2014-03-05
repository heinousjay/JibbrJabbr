package jj.script;

import jj.resource.ResourceFinder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class MakeRequireFunctionTest {

	@Mock CurrentScriptEnvironment env;
	@Mock ResourceFinder resourceFinder;
	
	@InjectMocks MakeRequireFunction mrf;
	
	
	@Test
	public void test() {
		System.out.println(mrf.script());
	}

}
