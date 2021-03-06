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

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import jj.util.SHA1Helper;

/**
 * internal helper to manage a saved SHA-1 hash for large files, so we only read it once
 * 
 * @author jason
 *
 */
class Sha1Resource extends AbstractResource<Sha1ResourceTarget> {
	
	static final String EXTENSION = "sha1";
	
	// empirical magic numbers - the sha1 hash is 40 hex digits, and the max long value as a decimal is 19 digits
	private static final Pattern FORMAT = Pattern.compile("^([a-f\\d]{40})(\\d{1,19})$");

	// 59 is the maximum size of the contents as described above.
	private static final int MAX_SIZE = 59;
	
	private final String representedSha;
	private final long representedFileSize;
	
	private final String sha1;
	
	private final Sha1ResourceTarget target;

	@Inject
	Sha1Resource(
		final Dependencies dependencies,
		final Sha1ResourceTarget target
	) throws IOException {
		super(dependencies);

		Path path = Paths.get(target.resource.path().toString() + "." + EXTENSION);
		
		// 3 possibilities
		// either there is no file at path, so we read in our target bytes to make one
		// or there is a file, but it's out of date, so we read in our target bytes and make a new one
		// or it's all good and we use it

		String sha = null;
		long size = -1;
		
		if (Files.exists(path)) {
			byte[] bytes = Files.readAllBytes(path);
			if (bytes.length <= MAX_SIZE) {
				Matcher matcher = FORMAT.matcher(new String(bytes, US_ASCII));
				if (matcher.matches()) {
					sha = matcher.group(1);
					size = Long.parseLong(matcher.group(2));
				}
			}
		}
	
		if (size != target.resource.size()) {
			sha = SHA1Helper.keyFor(target.resource.path());
			size = target.resource.size();
			Files.write(path, (sha + size).getBytes(US_ASCII));
		}
		
		representedSha = sha;
		representedFileSize = size;
		
		sha1 = SHA1Helper.keyFor(representedSha, String.valueOf(size));
		
		this.target = target;
		target.resource.addDependent(this);
	}
	
	@Override
	protected String extension() {
		return EXTENSION;
	}
	
	public String representedSha() {
		return representedSha;
	}

	public long representedFileSize() {
		return representedFileSize;
	}
	
	@Override
	public Sha1ResourceTarget creationArg() {
		return target;
	}

	@Override
	public String sha1() {
		return sha1;
	}

	@Override
	public boolean needsReplacing() throws IOException {
		// we only get replaced as a result of our master file going out of scope
		// i think, anyway
		return false;
	}
}