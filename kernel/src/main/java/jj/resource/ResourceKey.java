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

/**
 * <p>
 * Identifies a {@link Resource} in the {@link ResourceCache}.
 * 
 * <p>
 * Should always be created via the specific {@link ResourceCreator}
 * for the given {@link Resource} type. 
 * 
 * <p>
 * Do not rely on implementation details of this class. it is public
 * to facilitate efficient resource integrations, however, it is
 * intentionally opaque to support making changes in the resource system.
 * Details can (and probably will) change during any release. In
 * particular, do not rely on the string representation containing
 * any particular text
 * 
 * @author jason
 *
 */
public class ResourceKey {
	
	private final Class<? extends Resource<?>> type;
	private final URI uri;
	private final String toString;
	private final int hashCode;

	ResourceKey(final Class<? extends Resource<?>> type, final URI uri) {
		this.type = type;
		this.uri = uri;
		this.toString = type.getSimpleName().toString() + " at " + uri.toString();
		this.hashCode = toString.hashCode();
	}
	
	Class<? extends Resource<?>> type() {
		return type;
	}
	
	URI uri() {
		return uri;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof ResourceKey && toString.equals(String.valueOf(obj));
	}
	
	@Override
	public String toString() {
		return toString;
	}
}
