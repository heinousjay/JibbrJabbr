package jj.execution;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import java.util.concurrent.ScheduledExecutorService;
import jj.execution.ScriptExecutorFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScriptExecutorFactoryTest {

	ScriptExecutorFactory scriptExecutorFactory;
	@Mock JJTaskCreator taskCreator;
	@Mock JJScheduledTask<?> jjScheduledTask;
	
	@Before
	public void before() {
		scriptExecutorFactory = new ScriptExecutorFactory(taskCreator);
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
