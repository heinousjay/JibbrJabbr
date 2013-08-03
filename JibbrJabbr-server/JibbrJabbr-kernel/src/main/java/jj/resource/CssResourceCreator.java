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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.SHA1Helper;
import jj.configuration.Configuration;

/**
 * @author jason
 *
 */
@Singleton
class CssResourceCreator extends AbstractResourceCreator<CssResource> {
	
	private static final Pattern DOT_CSS = Pattern.compile("\\.css$");
	private static final Pattern DOT_LESS = Pattern.compile("\\.less$");
	
	private final Configuration configuration;
	private final LessProcessor lessProcessor;
	
	@Inject
	CssResourceCreator(final Configuration configuration, final LessProcessor lessProcessor) {
		this.configuration = configuration;
		this.lessProcessor = lessProcessor;
	}

	@Override
	public Class<CssResource> type() {
		return CssResource.class;
	}
	
	private String toLess(final String baseName) {
		return DOT_CSS.matcher(baseName).replaceFirst(".less");
	}
	
	@Override
	public boolean canLoad(String name, Object... args) {
		return (args.length == 1 && Boolean.TRUE.equals(args[0])) ?
			DOT_LESS.matcher(name).find() :
			DOT_CSS.matcher(name).find();
	}
	
	
	/**
	 * takes one parameter, if Boolean.TRUE then this tries to load a .less file
	 */
	@Override
	Path path(final String baseName, Object... args) {
		
		if (args != null && args.length == 1 && Boolean.TRUE.equals(args[0])) {
			return configuration.basePath().resolve(toLess(baseName));
			
		}
		return configuration.basePath().resolve(baseName);
	}

	@Override
	public CssResource create(String baseName, Object... args) throws IOException {
		boolean less = args.length == 1 && Boolean.TRUE.equals(args[0]);
		CssResource resource = new CssResource(cacheKey(baseName, args), baseName, path(baseName, args), less);
		
		if (less) {
			String processed = fixUrls(lessProcessor.process(toLess(baseName)));
			
			byte[] bytes = processed.getBytes(UTF_8);
			resource.byteBuffer.clear().writeBytes(bytes);
			resource.sha1 = SHA1Helper.keyFor(resource.byteBuffer);
		} else {
			
		}
		
		return resource;
	}
	
	private String fixUrls(String css) {
		return css;
	}

}
