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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.Sha1ResourceCreator.Sha1ResourceTarget;
import jj.util.SHA1Helper;

/**
 * internal helper to manage a saved SHA-1 hash for large files, so we only read it once
 * 
 * @author jason
 *
 */
@Singleton
class Sha1Resource extends AbstractResource {
	
	// empirical magic numbers - the sha1 hash is 40 hex digits, and the max long value as a decimal is 19 digits
	private static final Pattern FORMAT = Pattern.compile("^([a-f\\d]{40})(\\d{1,19})$");
	
	private final String representedSha;
	private final long representedFileSize;
	
	private final String sha1;

	@Inject
	Sha1Resource(
		final Dependencies dependencies,
		final Path path,
		final Sha1ResourceTarget target
	) throws IOException {
		super(dependencies);
		
		// 3 possibilities
		// either there is no file at path, so we read in our target bytes to make one
		// or there is a file, but it's out of date, so we read in our target bytes and make a new one
		// or it's all good and we use it
		
		// 59 is the maximum size of the contents as described above.
		ByteBuf byteBuffer = Unpooled.buffer(59, 59);
		String sha = null;
		long size = -1;
		
		if (Files.exists(path)) {
			byteBuffer.writeBytes(Files.readAllBytes(path));
			Matcher matcher = FORMAT.matcher(byteBuffer.toString(US_ASCII));
			if (!matcher.matches()) {
				throw new AssertionError("someone messed with the contents of Sha1Resource file");
			}
			sha = matcher.group(1);
			size = Long.parseLong(matcher.group(2));
		} else {
			sha = SHA1Helper.keyFor(target.resource.path());
			size = target.resource.size();
			
			Files.write(path, (sha + size).getBytes(US_ASCII));
		}
		
		// yuckerdo! but java makes this hard to extract
		// TODO - make this nicer. you have a test to validate it and everything
		if (size != target.resource.size()) {
			sha = SHA1Helper.keyFor(target.resource.path());
			size = target.resource.size();
			
			Files.write(path, (sha + size).getBytes(US_ASCII));
		}
		
		representedSha = sha;
		representedFileSize = size;
		
		sha1 = SHA1Helper.keyFor(representedSha, String.valueOf(size));
		
		target.resource.addDependent(this);
	}

	@Override
	public String uri() {
		return "/" + name + Sha1ResourceCreator.EXTENSION;
	}
	
	public String representedSha() {
		return representedSha;
	}

	public long representedFileSize() {
		return representedFileSize;
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
