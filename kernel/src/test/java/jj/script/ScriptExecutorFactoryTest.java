package jj.script;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import javax.inject.Provider;

import jj.script.ScriptEnvironment;
import jj.script.ScriptExecutorFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;



@RunWith(MockitoJUnitRunner.class)
public class ScriptExecutorFactoryTest {
	
	@Mock ScriptEnvironment scriptEnvironment1;
	@Mock ScriptEnvironment scriptEnvironment2;
	
	@Mock Provider<ScriptExecutor> provider;
	
	@Mock ScriptExecutor executor1;
	
	ScriptExecutorFactory scriptExecutorFactory;
	
	
	@Before
	public void before() {
		
		given(provider.get()).willReturn(executor1);
		
		scriptExecutorFactory = new ScriptExecutorFactory(provider);
	}
	
	@Test
	public void testSameScriptEnvironmentReturnsSameExecutor() {
		
		// need to verify that the same executor is returned for a given scriptEnvironment
		ScriptExecutor index1 = scriptExecutorFactory.executorFor(scriptEnvironment1);
		ScriptExecutor index2 = scriptExecutorFactory.executorFor(scriptEnvironment1);
		
		assertThat(index1, is(index2));
		
		ScriptExecutor other1 = scriptExecutorFactory.executorFor(scriptEnvironment2);
		ScriptExecutor other2 = scriptExecutorFactory.executorFor(scriptEnvironment2);
		
		assertThat(other1, is(other2));
	}

}
