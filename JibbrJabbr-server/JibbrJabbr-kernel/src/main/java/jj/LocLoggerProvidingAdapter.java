/*
 *    Copyright 2012 Jason Miller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jj;

import static jj.KernelMessages.*;

import java.lang.reflect.Type;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.injectors.FactoryInjector;
import org.picocontainer.injectors.InjectInto;

import org.slf4j.cal10n.LocLogger;
import org.slf4j.cal10n.LocLoggerFactory;

import ch.qos.cal10n.MessageConveyor;

/**
 * Adapter to provide Class-specific Logger instances from an SLF4J
 * LoggerFactory.
 * @author jason
 *
 */
final class LocLoggerProvidingAdapter extends FactoryInjector<LocLogger> {

	private static final String UNKNOWN_LOGGER_NAME = "jj.Unknown";
	
	@Override
	public LocLogger getComponentInstance(PicoContainer container, Type into)
			throws PicoCompositionException {
		
		MessageConveyor messageConveyor = container.getComponent(MessageConveyor.class);
		
		LocLoggerFactory factory = new LocLoggerFactory(messageConveyor);
		
		LocLogger logger = factory.getLocLogger(LocLoggerProvidingAdapter.class);
		
		if (into == null || 
			into == ComponentAdapter.NOTHING.class) {
			logger.warn(UsingUnknownLogger);
			factory.getLocLogger(UNKNOWN_LOGGER_NAME);
		}
		
		String loggerType = into.toString();
		if (into.getClass() == InjectInto.class) {
			loggerType = ((InjectInto)into).getIntoClass().getName();
		}
		
		logger.trace(ReturningLogger, loggerType);
		
		return factory.getLocLogger(loggerType);
	}

}
