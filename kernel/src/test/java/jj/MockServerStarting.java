package jj;

import static org.mockito.BDDMockito.mock;
import jj.execution.JJTask;

public class MockServerStarting extends ServerStarting {

	public MockServerStarting() {
		super(null, mock(Version.class));
	}
	
	public Priority priority;
	public JJTask task;

	@Override
	public void registerStartupTask(Priority priority, JJTask task) {
		this.priority = priority;
		this.task = task;
	}
}
