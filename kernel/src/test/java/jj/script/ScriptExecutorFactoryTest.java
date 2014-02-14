package jj.script;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ScheduledExecutorService;

import jj.execution.JJRejectedExecutionHandler;
import jj.execution.JJThreadFactory;
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
	
	@Mock UncaughtExceptionHandler uncaughtExceptionHandler;

	ScriptExecutorFactory scriptExecutorFactory;
	
	
	@Before
	public void before() {
		scriptExecutorFactory = new ScriptExecutorFactory(new JJThreadFactory(uncaughtExceptionHandler), new JJRejectedExecutionHandler());
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
