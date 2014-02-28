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

import java.util.HashSet;
import java.util.Set;

import jj.BootstrapClassPath;

/**
 * @author jason
 *
 */
public class TestableAssets extends Assets {
	
	private static final Set<String> paths;
	
	static {
		
		paths = new HashSet<>();
		paths.add("/jj/assets/");
	}

	
	public TestableAssets() {
		super(new BootstrapClassPath(), paths);
	}

}
