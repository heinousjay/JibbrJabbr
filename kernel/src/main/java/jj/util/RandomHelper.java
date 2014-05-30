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

import java.util.Random;

/**
 * wraps 
 * 
 * @author jason
 *
 */
public enum RandomHelper {
	
	;
	
	private static ThreadLocal<Random> randoms = new ThreadLocal<Random>() {
		protected Random initialValue() {
			return new Random();
		};
	};
	
	public static int nextInt() {
		return randoms.get().nextInt();
	}
	
	public static int nextInt(int limit) {
		return randoms.get().nextInt(limit);
	}

	/**
	 * @param i
	 * @param j
	 * @return
	 */
	public static int nextInt(int i, int j) {
		if (i > j) {
			return randoms.get().nextInt(i - j) + j;
		}
		return randoms.get().nextInt(j - i) + i;
	}
}
