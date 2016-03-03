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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Exposes system version information as constants.
 * 
 * @author Jason Miller
 *
 */
public class Version {
	
	/** The name of the system */
	private final String name;
	
	/** The full version of the system */
	private final String version;
	
	/** The major version of the system */
	private final int major;
	
	/** The minor version of the system */
	private final int minor;
	
	/** Flag indicating if this is a snapshot */
	private final boolean snapshot;
	
	/** name of the repository branch for this build version */
	private final String branchName;
	
	/** user name of the person who created the commit for this build version */
	private final String commitUserName;
	
	/** email address of the person who created the commit for this build version */
	private final String commitUserEmail;
	
	/** id of the commit for this build version */
	private final String commitId;
	
	/** description of the commit for this build version */
	private final String commitDescription;
	
	/** date and time of the commit for this build version */
	private final Instant commitDate; 
	
	Version() {
		// we do things this way to avoid depending on any jj internal classes in
		// order to create these values
		try (BufferedReader r = new BufferedReader(
			new InputStreamReader(Version.class.getResourceAsStream("VERSION"), StandardCharsets.UTF_8)
		)
		) {
		
			name = r.readLine();
			version = r.readLine();
			
			Pattern versionParser = Pattern.compile("(\\d*)\\.(\\d*)(-SNAPSHOT)?");
			
			Matcher matcher = versionParser.matcher(version);
			if (matcher.matches()) {
				major = Integer.parseInt(matcher.group(1));
				minor = Integer.parseInt(matcher.group(2));
				snapshot = matcher.group(3) != null;
			} else {
				major = 0;
				minor = 0;
				snapshot = true;
			}
			
			branchName = r.readLine();
			
			commitUserName = r.readLine();
			commitUserEmail = r.readLine();
			commitId = r.readLine();
			commitDescription = r.readLine();
			
			Instant candidate;
			try {
				candidate = Instant.ofEpochMilli(Long.parseLong(r.readLine()));
			} catch (Exception e) {
				candidate = Instant.EPOCH;
			}
			commitDate = candidate;
			
		} catch (IOException e) {
			throw new AssertionError("MY JAR IS BROKEN", e);
		}
	}
	
	public String name() {
		return name;
	}
	
	public String version() {
		return version;
	}
	
	public int major() {
		return major;
	}
	
	public int minor() {
		return minor;
	}
	
	public boolean snapshot() {
		return snapshot;
	}
	
	public String branchName() {
		return branchName;
	}
	
	public String commitId() {
		return commitId;
	}
	
	public String commitUserName() {
		return commitUserName;
	}
	
	public String commitUserEmail() {
		return commitUserEmail;
	}
	
	public String commitDescription() {
		return commitDescription;
	}
	
	public Instant commitDate() {
		return commitDate;
	}
}
