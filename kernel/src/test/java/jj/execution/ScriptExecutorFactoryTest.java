package jj.execution;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import java.util.concurrent.ScheduledExecutorService;
import jj.execution.ScriptExecutorFactory;

import org.junit.Before;
import org.junit.Test;

public class ScriptExecutorFactoryTest {

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
	public void testSameBaseNameReturnsSameExecutor() {
		
		// need to verify that the same executor is returned for a given baseName
		
		String baseName = "index";
		ScheduledExecutorService index1 = scriptExecutorFactory.executorFor(baseName);
		ScheduledExecutorService index2 = scriptExecutorFactory.executorFor(baseName);
		
		assertThat(index1, is(index2));
		
		String baseName2 = "other";
		ScheduledExecutorService other1 = scriptExecutorFactory.executorFor(baseName2);
		ScheduledExecutorService other2 = scriptExecutorFactory.executorFor(baseName2);
		
		assertThat(other1, is(other2));
	}

}