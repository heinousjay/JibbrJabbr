package jj.execution;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.concurrent.ScheduledExecutorService;

import jj.execution.ScriptExecutorFactory;
import jj.script.ScriptEnvironment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;



@RunWith(MockitoJUnitRunner.class)
public class ScriptExecutorFactoryTest {
	
	@Mock ScriptEnvironment scriptEnvironment1;
	@Mock ScriptEnvironment scriptEnvironment2;

	ScriptExecutorFactory scriptExecutorFactory;
	
	
	@Before
	public void before() {
		scriptExecutorFactory = new ScriptExecutorFactory(new Thread.UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	@Test
	public void testSameScriptEnvironmentReturnsSameExecutor() {
		
		// need to verify that the same executor is returned for a given scriptEnvironment
		ScheduledExecutorService index1 = scriptExecutorFactory.executorFor(scriptEnvironment1);
		ScheduledExecutorService index2 = scriptExecutorFactory.executorFor(scriptEnvironment1);
		
		assertThat(index1, is(index2));
		
		ScheduledExecutorService other1 = scriptExecutorFactory.executorFor(scriptEnvironment2);
		ScheduledExecutorService other2 = scriptExecutorFactory.executorFor(scriptEnvironment2);
		
		assertThat(other1, is(other2));
	}

}
