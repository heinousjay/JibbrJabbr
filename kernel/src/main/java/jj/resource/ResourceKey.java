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
 * @author jason
 *
 */
public class ResourceKey {
	
	private final String toString;
	private final int hashCode;

	public ResourceKey(final Class<? extends Resource> type, final URI uri) {
		this.toString = type.getSimpleName().toString() + " at " + uri.toString();
		this.hashCode = toString.hashCode();
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