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
package jj.messaging;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Location;
import jj.configuration.resolution.AppLocation;
import jj.resource.AbstractResourceCreator;
import jj.resource.ResourceInstanceCreator;

/**
 * @author jason
 *
 */
@Singleton
class MessagesResourceCreator extends AbstractResourceCreator<MessagesResource> {

	private final ResourceInstanceCreator creator;
	
	@Inject
	MessagesResourceCreator(
		final ResourceInstanceCreator creator
	) {
		this.creator = creator;
	}

	@Override
	public MessagesResource create(Location base, String name, Object... args) throws IOException {
		assert args.length == 1 && args[0] instanceof Locale : "MessagesResource requires a Locale argument";
		assert base == AppLocation.Virtual : "MessagesResource is only Virtual";
		return creator.createResource(MessagesResource.class, resourceKey(base, name, args), base, name, args);
	}

	@Override
	protected URI uri(Location base, String name, Object... args) {
		assert args.length == 1 && args[0] instanceof Locale : "messages require a Locale argument";
		return URI.create(name + "#" + String.valueOf(args[0]));
	}

}
