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
package jj.css;

import java.net.URI;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.Location;
import jj.resource.SimpleResourceCreator;
import jj.server.ServerLocation;

/**
 * @author jason
 *
 */
@Singleton
class StylesheetResourceCreator extends SimpleResourceCreator<StylesheetResource, Void> {
	
	@Inject
	StylesheetResourceCreator(final Dependencies dependencies) {
		super(dependencies);
	}
	
	@Override
	protected URI uri(Location base, String name, Void argument) {
		assert base == ServerLocation.Virtual;
		return URI.create(name);
	}
}
