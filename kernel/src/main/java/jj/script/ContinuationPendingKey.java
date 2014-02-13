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
package jj.script;

import javax.inject.Inject;

import jj.JJ;
import jj.SecureRandomHelper;
import jj.StringUtils;

/**
 * simple object to represent continuation pending keys
 * 
 * for now, just wraps a simple string id, which can be specified
 * 
 * @author jason
 *
 */
public class ContinuationPendingKey {

	private final String id;
	
	private final String toString;
	
	@Inject
	ContinuationPendingKey(final ContinuationPendingCache cache) {
		this.id = cache.uniqueID();
		this.toString = makeToString();
	}
	
	public ContinuationPendingKey() {
		assert !JJ.isRunning : "DO NOT USE THIS CONSTRUCTOR IN THE RUNNING SYSTEM!";
		// I'm sure that most of the time this would be fine.  it's the one-in-a-billion 
		// or whatever shot at it being wrong that i hate
		id = Integer.toHexString(SecureRandomHelper.nextInt());
		toString = makeToString();
	}
	
	public ContinuationPendingKey(final String id) {
		assert !StringUtils.isEmpty(id);
		this.id = id;
		this.toString = makeToString();
	}
	
	private String makeToString() {
		return getClass().getSimpleName() + "-" + id;
	}
	
	public String id() {
		return id;
	}
	
	public boolean equals(Object obj) {
		
		return obj != null &&
			obj instanceof ContinuationPendingKey &&
			((ContinuationPendingKey)obj).id.equals(id);
	}
	
	@Override
	public int hashCode() {
		return toString.hashCode();
	}
	
	public String toString() {
		return toString;
	}
}
