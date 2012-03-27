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

import java.lang.reflect.Type;
import java.util.Locale;

import org.picocontainer.PicoContainer;
import org.picocontainer.injectors.FactoryInjector;

import ch.qos.cal10n.MessageConveyor;

/**
 * @author Jason Miller
 *
 */
public class MessageConveyorProvidingAdapter extends FactoryInjector<MessageConveyor> {

	private final MessageConveyor messageConveyor;
	
	public MessageConveyorProvidingAdapter() {
		// need to decide if the default locale can be resolved, and if not, then
		// we default to English.  but later.
		messageConveyor = new MessageConveyor(Locale.getDefault());
	}
	
	@Override
	public MessageConveyor getComponentInstance(PicoContainer container, Type into) {
		return messageConveyor;
	}

}
