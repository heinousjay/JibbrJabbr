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
package jj.resource.sha1;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.AppLocation;
import jj.resource.AbstractFileResource;
import jj.resource.MimeTypes;
import jj.resource.ResourceCacheKey;

/**
 * @author jason
 *
 */
@Singleton
public class Sha1Resource extends AbstractFileResource {
	
	private static final Pattern FORMAT = Pattern.compile("^([a-f\\d]{40})(-?\\d{1,14})$");
	
	private final String representedSha;
	private final FileTime representedFileTime;

	/**
	 * @param cacheKey
	 * @param baseName
	 * @param path
	 * @throws IOException
	 */
	@Inject
	Sha1Resource(ResourceCacheKey cacheKey, AppLocation base, String name, Path path) throws IOException {
		super(cacheKey, base, name, path);
		Matcher matcher = FORMAT.matcher(byteBuffer.toString(UTF_8));
		if (!matcher.matches()) {
			throw new NoSuchFileException(path.toString());
		}
		representedSha = matcher.group(1);
		representedFileTime = FileTime.fromMillis(Long.parseLong(matcher.group(2)));
	}

	@Override
	public String uri() {
		return "/";
	}

	@Override
	public String mime() {
		return MimeTypes.get(baseName);
	}
	
	public String representedSha() {
		return representedSha;
	}

	public FileTime representedFileTime() {
		return representedFileTime;
	}
}
