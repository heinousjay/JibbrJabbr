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
package jj.i18n;

import static jj.system.ServerLocation.Virtual;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.SimpleResourceCreator;
import jj.resource.Location;

/**
 * @author jason
 *
 */
@Singleton
class MessagesResourceCreator extends SimpleResourceCreator<MessagesResource> {

	@Inject
	MessagesResourceCreator(final Dependencies dependencies) {
		super(dependencies);
	}

	@Override
	public MessagesResource create(Location base, String name, Object... args) throws IOException {
		assertArgs(base, args);
		return creator.createResource(MessagesResource.class, resourceKey(base, name, args), base, name, args);
	}

	@Override
	protected URI uri(Location base, String name, Object... args) {
		assertArgs(base, args);
		Locale locale = (Locale)args[0];
		return URI.create(name + "_" + locale.toLanguageTag());
	}

	private void assertArgs(Location base, Object... args) {
		assert args.length == 1 && args[0] instanceof Locale : "messages require a Locale argument";
		assert base == Virtual : "MessagesResource must be Virtual";
	}

}
