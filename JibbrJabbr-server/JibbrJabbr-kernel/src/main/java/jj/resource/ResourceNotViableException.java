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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author jason
 *
 */
public class ResourceNotViableException extends RuntimeException {
	
	private final static Set<String> filteredPackages;
	
	static {
		HashSet<String> work = new HashSet<>();
		work.add("java.util.concurrent");
		work.add("sun.reflect");
		work.add("java.lang.reflect");
		work.add("com.google.inject");
		work.add("java.lang.Thread");
		
		filteredPackages = Collections.unmodifiableSet(work);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6562864002411119175L;
	
	{
		List<StackTraceElement> list = new ArrayList<>();
		for (StackTraceElement ste : getStackTrace()) {
			boolean add = true;
			for (String filteredPackage : filteredPackages) {
				if (ste.getClassName().startsWith(filteredPackage)) {
					add = false;
					break;
				}
			}
			if (add) {
				list.add(ste);
			}
		}
		
		setStackTrace(list.toArray(new StackTraceElement[list.size()]));
	}
	
	public ResourceNotViableException(String uri) {
		super(uri);
	}

	public ResourceNotViableException(Path resourcePath) {
		super(resourcePath.toAbsolutePath().toString());
	}

	public ResourceNotViableException(Path resourcePath, String additionalMessage) {
		super(String.format("resource at %s could not be loaded because %s", resourcePath.toAbsolutePath().toString(), additionalMessage));
	}
	
	/**
	 * @param message
	 * @param cause
	 */
	public ResourceNotViableException(Path resourcePath, Throwable cause) {
		super(resourcePath.toAbsolutePath().toString(), cause);
	}
	
	public ResourceNotViableException(String uri, Throwable cause) {
		super(uri, cause);
	}
}
