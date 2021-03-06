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
import jj.util.SecureRandomHelper;
import jj.util.StringUtils;

/**
 * Represents a key to restarting a paused script execution
 * 
 * @author jason
 *
 */
public class PendingKey {

	/**
	 * MUST BE UNPREDICTABLE as this is exposed to
	 * the outside world
	 */
	private final String id;
	
	private final String toString;
	
	private final ContinuationPendingCache cache;
	
	@Inject
	PendingKey(final ContinuationPendingCache cache) {
		this.id = cache.uniqueID();
		this.toString = makeToString();
		this.cache = cache;
	}
	
	public PendingKey() {
		assert !JJ.isRunning : "DO NOT USE THIS CONSTRUCTOR IN THE RUNNING SYSTEM!  TEST ONLY!";
		// I'm sure that most of the time this would be fine instead of bothering to ensure the key is
		// unique within the system.  it's the one-in-a-billion 
		// or whatever shot at it being wrong that i hate leaving in there.  the key to robust behavior
		// is leaving none of this to chance
		id = Integer.toHexString(SecureRandomHelper.nextInt());
		toString = makeToString();
		cache = new ContinuationPendingKeyResultExtractorHelper();
	}
	
	public PendingKey(final String id) {
		assert !StringUtils.isEmpty(id);
		this.id = id;
		this.toString = makeToString();
		cache = null;
	}
	
	private String makeToString() {
		return getClass().getSimpleName() + "-" + id;
	}
	
	public String id() {
		return id;
	}
	
	public boolean equals(Object obj) {
		
		return obj != null &&
			obj instanceof PendingKey &&
			((PendingKey)obj).id.equals(id);
	}
	
	@Override
	public int hashCode() {
		return toString.hashCode();
	}
	
	public String toString() {
		return toString;
	}

	public void resume(Object result) {
		assert cache != null;
		cache.resume(this, result);
	}
}
