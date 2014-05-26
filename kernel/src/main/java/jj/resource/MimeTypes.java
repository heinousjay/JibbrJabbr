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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jason
 *
 */
public class MimeTypes {

	/**
	 * 
	 */
	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
	private static final Map<String, String> mimeTypes;
	
	static {
		
		Map<String, String> mimeTypesMaker = new HashMap<String, String>();
		mimeTypesMaker.put("htm", "text/html; charset=UTF-8");
		mimeTypesMaker.put("html", "text/html; charset=UTF-8");
		mimeTypesMaker.put("js", "text/javascript; charset=UTF-8");
		mimeTypesMaker.put("css", "text/css; charset=UTF-8");
		mimeTypesMaker.put("txt", "text/plain; charset=UTF-8");
		mimeTypesMaker.put("sha1", "text/plain; charset=UTF-8");
		mimeTypesMaker.put("csv", "text/csv; charset=UTF-8");
		mimeTypesMaker.put("xml", "text/xml; charset=UTF-8");
		mimeTypesMaker.put("xhtml", "application/xhtml+xml");
		mimeTypesMaker.put("json", "application/json");
		mimeTypesMaker.put("pdf", "application/pdf");
		mimeTypesMaker.put("zip", "application/zip");
		mimeTypesMaker.put("tar", "application/x-tar");
		mimeTypesMaker.put("gif", "image/gif");
		mimeTypesMaker.put("jpeg", "image/jpeg");
		mimeTypesMaker.put("jpg", "image/jpeg");
		mimeTypesMaker.put("tiff", "image/tiff");
		mimeTypesMaker.put("tif", "image/tiff");
		mimeTypesMaker.put("png", "image/png");
		mimeTypesMaker.put("svg", "image/svg+xml");
		mimeTypesMaker.put("ico", "image/vnd.microsoft.icon");
		mimeTypes = Collections.unmodifiableMap(mimeTypesMaker);
	}
	

	private static String extension(final String path) {
		return path.substring(path.lastIndexOf('.') + 1);
	}

	public static String get(final String path) {
		final String extension = extension(path);
		return mimeTypes.containsKey(extension) ? mimeTypes.get(extension) : DEFAULT_MIME_TYPE;
	}

	/**
	 * @return
	 */
	public static String getDefault() {
		return DEFAULT_MIME_TYPE;
	}
}
