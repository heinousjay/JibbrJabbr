package jj;

import java.util.Locale;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.cal10n.LocLogger;

import ch.qos.cal10n.MessageConveyor;

/**
 * This is overblown atm
 * @author jason
 *
 */
public class CoreResourcesRule implements TestRule {

	private static final MessageConveyor messageConveyor = new MessageConveyor(Locale.US);
	private static final MockLogger mockLogger = new MockLogger();
	private static final LocLogger locLogger = new LocLogger(mockLogger, messageConveyor);
	
	public MessageConveyor messageConveyor() {
		return messageConveyor;
	}
	
	public MockLogger mockLogger() {
		return mockLogger;
	}
	
	public LocLogger logger() {
		return locLogger;
	}
	
	@Override
	public Statement apply(final Statement base, final Description description) {

		return base;
	}

}
