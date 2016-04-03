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
package jj.event;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;

import jj.JJModule;

/**
 * @author jason
 *
 */
public class EventModule extends JJModule {

	private static class SubscriberMatcher extends AbstractMatcher<TypeLiteral<?>> {

		@Override
		public boolean matches(TypeLiteral<?> t) {
			return t.getRawType().isAnnotationPresent(Subscriber.class);
		}
	}
	
	@Override
	protected void configure() {

		// since the EventListenerBinder cannot be injected, we create the
		// state here and bind it as an instance.
		EventSystemState state = new EventSystemState();
		bind(EventSystemState.class).toInstance(state);
		bindListener(new SubscriberMatcher(), new EventListenerBinder(state));

		bind(Publisher.class).to(PublisherImpl.class);
		bind(ListenerReferenceCleaner.class).asEagerSingleton();
	}

}
