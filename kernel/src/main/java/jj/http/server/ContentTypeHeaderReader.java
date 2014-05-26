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
package jj.http.server;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import jj.util.StringUtils;
import io.netty.handler.codec.http.HttpHeaders;

/**
 * @author jason
 *
 */
class ContentTypeHeaderReader {
	
	private static final String MULTIPART = "multipart";
	
	private static final String TEXT = "text";
	
	private static final String CHARSET = "charset";
	
	private static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;
	
	private final MimeType value;
	
	private final boolean badRequest;
	
	private final Charset charset;
	
	private final boolean unsupportedMediaType;

	ContentTypeHeaderReader(final HttpHeaders httpHeaders) {
		String headerValue = httpHeaders.get(HttpHeaders.Names.CONTENT_TYPE);
		assert headerValue != null : "can't read a nonexistent header";
		value = mimeType(headerValue);
		badRequest = value == null;
		charset = isText() ? findCharset() : null;
		unsupportedMediaType = isText() && charset == null;
	}
	
	private MimeType mimeType(String headerValue) {
		try {
			return new MimeType(headerValue);
		} catch (MimeTypeParseException e) {
			return null;
		}
	}
	
	public boolean isBadRequest() {
		return badRequest;
	}
	
	public boolean isUnsupportedMediaType() {
		return unsupportedMediaType;
	}
	
	public boolean isMultipart() {
		return !badRequest && MULTIPART.equals(value.getPrimaryType());
	}
	
	public boolean isText() {
		return !badRequest && TEXT.equals(value.getPrimaryType());
	}
	
	public String mimeType() {
		return badRequest ? null : value.getBaseType();
	}
	
	public Charset charset() {
		return charset;
	}
	
	private Charset findCharset() {
		String name = value.getParameter(CHARSET);
		if (StringUtils.isEmpty(name)) {
			return DEFAULT_CHARSET;
		}
		try {
			return Charset.forName(name);
		} catch (Exception e) {
			return null;
		}
	}
}
