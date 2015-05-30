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

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import jj.util.StringUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;

/**
 * @author jason
 *
 */
class ContentTypeHeaderReader {
	
	private static final String MULTIPART = "multipart";
	
	private static final String CHARSET = "charset";
	
	private static final String BOUNDARY = "boundary";
	
	private final MimeType value;
	
	private final boolean badRequest;
	
	private final Charset charset;

	ContentTypeHeaderReader(final HttpHeaders httpHeaders) {
		CharSequence headerValue = httpHeaders.get(HttpHeaderNames.CONTENT_TYPE);
		assert headerValue != null : "can't read a nonexistent header";
		value = mimeType(headerValue);
		badRequest = value == null;
		charset = value != null ? findCharset() : null;
	}
	
	private MimeType mimeType(CharSequence headerValue) {
		try {
			return new MimeType(headerValue.toString());
		} catch (MimeTypeParseException e) {
			return null;
		}
	}
	
	public boolean isBadRequest() {
		return badRequest;
	}
	
	public boolean isMultipart() {
		return !badRequest && MULTIPART.equals(value.getPrimaryType());
	}
	
	public boolean isText() {
		return charset != null;
	}
	
	public String mimeType() {
		return badRequest ? null : value.getBaseType();
	}
	
	public Charset charset() {
		return charset;
	}
	
	public String boundary() {
		return value == null ? null : value.getParameter(BOUNDARY); 
	}
	
	private Charset findCharset() {
		String name = value.getParameter(CHARSET);
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		try {
			return Charset.forName(name);
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	public String toString() {
		return "value: " + value + ", charset: " + charset;
	}
}
