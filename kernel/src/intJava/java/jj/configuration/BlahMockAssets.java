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
package jj.configuration;

import jj.BootstrapClassPath;

/**
 * working around a classpath mismatch.  need to reformulate some stuff,
 * i think this is going back into its own project because i wanna make it
 * into a module anyway
 * @author jason
 *
 */
public class BlahMockAssets extends Assets {

	/**
	 * @param resolver
	 */
	public BlahMockAssets() {
		super(new BootstrapClassPath());
	}

}
