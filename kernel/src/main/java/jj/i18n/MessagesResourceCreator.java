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

import static jj.server.ServerLocation.Virtual;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.ResourceIdentifier;
import jj.resource.SimpleResourceCreator;
import jj.resource.Location;

/**
 * @author jason
 *
 */
@Singleton
class MessagesResourceCreator extends SimpleResourceCreator<Locale, MessagesResource> {

	@Inject
	MessagesResourceCreator(final Dependencies dependencies) {
		super(dependencies);
	}

	@Override
	public MessagesResource create(Location base, String name, Locale locale) throws IOException {
		assertArgs(base);
		return creator.createResource(resourceIdentifierMaker.make(type(), base, name, locale));
	}

	@Override
	protected URI uri(Location base, String name, Locale locale) {
		assertArgs(base);
		return URI.create(name + "_" + locale.toLanguageTag());
	}

	private void assertArgs(Location base) {
		assert base == Virtual : "MessagesResource must be Virtual";
	}

}
