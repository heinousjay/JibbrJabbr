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
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Exposes system version information as constants.
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
	
	/** name of the repository branch for this build version */
	public static final String branchName;
	
	/** user name of the person who created the commit for this build version */
	public static final String commitUserName;
	
	/** email address of the person who created the commit for this build version */
	public static final String commitUserEmail;
	
	/** id of the commit for this build version */
	public static final String commitId;
	
	/** description of the commit for this build version */
	public static final String commitDescription;
	
	/** date and time of the commit for this build version */
	public static final Date commitDate;
	
	/** user name of the person who built this version */
	public static final String buildUserName;
	
	/** email address of the person who built this version */
	public static final String buildUserEmail;
	
	/** date and time when this version was built */
	public static final Date buildDate;
	
	static {
		// we do things this way to avoid depending on any jj internal classes in
		// order to create these values.  The jj.api package is intended to have
		// no visibility into system internals.
		
		try (BufferedReader r = new BufferedReader(
				new InputStreamReader(
					Version.class.getResourceAsStream("VERSION"), StandardCharsets.UTF_8
				)
			)
		) {
			
			name = r.readLine();
			version = r.readLine();
			
			Pattern versionParser = Pattern.compile("(\\d*)\\.(\\d*)(-SNAPSHOT)?");
			
			Matcher matcher = versionParser.matcher(version);
			matcher.matches();
			major = Integer.parseInt(matcher.group(1));
			minor = Integer.parseInt(matcher.group(2));
			snapshot = matcher.group(3) != null;
			
			branchName = r.readLine();
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy '@' HH:mm:ss z");
			
			commitUserName = r.readLine();
			commitUserEmail = r.readLine();
			commitId = r.readLine();
			commitDescription = r.readLine();
			commitDate = sdf.parse(r.readLine());
			
			buildUserName = r.readLine();
			buildUserEmail = r.readLine();
			buildDate = sdf.parse(r.readLine());

			
		} catch (Exception e) {
			throw new IllegalStateException("MY JAR IS BROKEN", e);
		}
	}
	
	private Version() {}
}
