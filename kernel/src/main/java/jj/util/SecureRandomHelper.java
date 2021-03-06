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

import java.security.SecureRandom;

/**
 * @author jason
 *
 */
public enum SecureRandomHelper {
	
	;
	
	private static ThreadLocal<SecureRandom> randoms = new ThreadLocal<SecureRandom>() {
		protected SecureRandom initialValue() {
			return new SecureRandom();
		}
	};
	
	public static int nextInt() {
		return randoms.get().nextInt();
	}
	
	public static byte[] nextBytes(int size) {
		byte[] result = new byte[size];
		randoms.get().nextBytes(result);
		return result;
	}
	
	public static long nextLong() {
		return randoms.get().nextLong();
	}
}
