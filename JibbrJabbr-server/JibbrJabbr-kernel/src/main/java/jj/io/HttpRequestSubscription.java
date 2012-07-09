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
package jj.io;

/**
 * 
 * 
 * @author jason
 *
 */
public abstract class HttpRequestSubscription {

	protected final String path;
	
	volatile boolean subscribe = true;
	
	public HttpRequestSubscription(final String path) {
		this.path = path;
	}
	
	public final void unsubscribe() {
		subscribe = false;
	}
	
	protected abstract void request();
	
	@Override
	public final boolean equals(Object other) {
		return other == this;
	}
	
	@Override
	public final int hashCode() {
		return System.identityHashCode(this);
	}
}
