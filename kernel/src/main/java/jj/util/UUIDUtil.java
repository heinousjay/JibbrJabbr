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

import java.util.UUID;

/**
 * <p>
 * provides access to UUID creation.  it appears the the java.util.UUID
 * shares a single global instance of SecureRandom, which in turn
 * synchronizes the getBytes method, dammit, so this util provides
 * access to a thread local random UUID generator
 * 
 * @author jason
 *
 */
public enum UUIDUtil {
	;
	
	private static final int longSize = 8;
	
	public static UUID newRandomUUID() {
		byte[] msbytes = SecureRandomHelper.nextBytes(longSize);
		byte[] lsbytes = SecureRandomHelper.nextBytes(longSize);
		
		// ugh, copied from the JDK class.  possible illegally
		msbytes[6]  &= 0x0f;  /* clear version        */
		msbytes[6]  |= 0x40;  /* set to version 4     */
		lsbytes[0]  &= 0x3f;  /* clear variant        */
		lsbytes[0]  |= 0x80;  /* set to IETF variant  */
		
		long msb = 0;
		for (int i=0; i < longSize; i++) {
			msb = (msb << 8) | (msbytes[i] & 0xff);
		}
		
		long lsb = 0;
		for (int i=0; i < longSize; i++) {
			lsb = (lsb << 8) | (lsbytes[i] & 0xff);
		}
		
		return new UUID(msb, lsb);
	}
}
