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
package jj;

import jj.configuration.Argument;
import jj.configuration.Default;

import java.nio.file.Path;

/**
 * @author jason
 *
 */
public interface CoreConfiguration {

	@Argument("app")
	Path appPath();
	

	/**
	 * Flag indicating that the client should be in debug mode, which
	 * will log internal info to the script console
	 * 
	 * TODO move this to a more appropriate configuration
	 * @return
	 */
	@Argument("debug")
	@Default("false")
	boolean debugClient();
}
