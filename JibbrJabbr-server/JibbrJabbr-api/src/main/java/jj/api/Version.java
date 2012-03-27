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
package jj.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Exposes the system version as constants.
 * 
 * @author Jason Miller
 *
 */
public final class Version {
	
	/** The name of the system */
	public static final String name;
	/** The full version of the system */
	public static final String version;
	/** The major version of the system */
	public static final int major;
	/** The minor version of the system */
	public static final int minor;
	/** Flag indicating if this is a snapshot */
	public static final boolean snapshot;
	
	private static final Pattern versionParser =
		Pattern.compile("(\\d*)\\.(\\d*)(-SNAPSHOT)?");
	
	static {
		// we do things this way to avoid depending on any jj internal classes in
		// order to create these values.  The jj.api package is intended to have
		// no visibility into system internals.
		
		try (BufferedReader r = new BufferedReader(
				new InputStreamReader(
					Version.class.getResourceAsStream("VERSION"), Charset.forName("UTF-8")
				)
			)
		) {
			
			name = r.readLine();
			version = r.readLine();
			
			Matcher matcher = versionParser.matcher(version);
			matcher.matches();
			major = Integer.parseInt(matcher.group(1));
			minor = Integer.parseInt(matcher.group(2));
			snapshot = matcher.group(3) != null;
			
		} catch (Exception e) {
			throw new IllegalStateException("MY JAR IS BROKEN", e);
		}
	}
}
