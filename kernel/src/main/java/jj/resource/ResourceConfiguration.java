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
package jj.resource;

import java.util.Map;

import jj.configuration.Default;

/**
 * @author jason
 *
 */
public interface ResourceConfiguration {

	// 10 megabytes is the default limit
	static final long MAX_IN_MEMORY_SIZE  = 1024 * 1024 * 10;
	
	/**
	 * The maximum number of IO workers that will be available.
	 * @return
	 */
	@Default("20")
	int ioThreads();
	
	@Default(MAX_IN_MEMORY_SIZE + "") // hi java! you suck sometimes!
	long maxFileSizeToLoad();
	
	// this isn't actually exposed for configuration, it's more or
	// less a constant
	@Default("application/octet-stream")
	String defaultMimeType();
	
	/**
	 * various settings for file-based resources, organized by extension
	 * @return
	 */
	Map<String, ResourceSettings> typeConfigurations();
}
