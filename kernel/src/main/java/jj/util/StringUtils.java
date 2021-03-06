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
package jj.util;

/**
 * Simple static utilities for safely handling strings that
 * may be null.  based on commons lang but copied here to
 * keep dependencies simple.  we bring in enough.
 * 
 * @author jason
 *
 */
public enum StringUtils {

	; // no instances
	
	/**
	 * null safe empty check.  null is empty!
	 * @param in
	 * @return
	 */
	public static boolean isEmpty(String in) {
		return in == null || in.isEmpty();
	}

	/**
	 * Null safe equality check
	 * @param uri
	 * @param uri2
	 * @return
	 */
	public static boolean equals(String uri, String uri2) {
		return (uri == uri2) || (uri != null && uri.equals(uri2));
	}
}
