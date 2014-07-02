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
package jj.css;

import jj.configuration.Default;

/**
 * <p>
 * Allows to configure less processing options
 * 
 * https://github.com/less/less-docs/blob/master/content/usage/command-line-usage.md
 * @author jason
 *
 */
public interface LessConfiguration {
	
	boolean compress();
	
	boolean cleancss();
	
	@Default("-1")
	int maxLineLen();
	
	@Default("1")
	int optimization();
	
	boolean silent();
	
	boolean verbose();
	
	boolean lint();
	
	@Default("true")
	boolean color();
	
	boolean strictImports();
	
	boolean relativeUrls();
	
	@Default("true")
	boolean ieCompat();
	
	boolean strictMath();
	
	boolean strictUnits();
	
	@Default("true")
	boolean javascriptEnabled();
	
	@Default("true")
	boolean sourceMaps();
	
	String rootpath(); // null by default
}
