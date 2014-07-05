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
package jj.resource;

import java.net.URI;

import jj.util.GenericUtils;

/**
 * <p>
 * Extend this to declare a {@link ResourceCreator}
 * 
 * @author jason
 *
 */
public abstract class AbstractResourceCreator<T extends AbstractResource> implements ResourceCreator<T> {

	protected abstract URI uri(final Location base, final String name, final Object...args);
	
	private final Class<T> type;
	
	@SuppressWarnings("unchecked")
	protected AbstractResourceCreator() {
		type = (Class<T>)GenericUtils.extractGenericParameter(getClass());
	}
	
	@Override
	public Class<T> type() {
		return type;
	}
	
	@Override
	public ResourceKey resourceKey(final Location base, final String name, final Object...args) {
		return new ResourceKey(type, uri(base, name, args));
	}
}
