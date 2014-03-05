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

import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * @author jason
 *
 */
public class App {
	
	public static final String one;
	
	public static final String two;
	
	public static final String minimal;
	
	static {
		try {
			
			for (URL resource : Collections.list(App.class.getClassLoader().getResources("/"))) {
				System.out.println(resource);
			}
			
			one = Paths.get(App.class.getResource("/app1/app/").toURI()).toAbsolutePath().toString();
			two = Paths.get(App.class.getResource("/app2/app/").toURI()).toAbsolutePath().toString();
			minimal = Paths.get(App.class.getResource("/minimal/app/").toURI()).toAbsolutePath().toString();
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
}
