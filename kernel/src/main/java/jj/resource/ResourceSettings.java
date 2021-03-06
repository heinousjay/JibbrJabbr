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

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * value object to configure resources according
 * to some organized higher-level key. supports
 * creating instances as overrides
 * 
 * @author jason
 *
 */
public class ResourceSettings {
	
	public static Charset makeCharset(final String charset) {
		try {
			return Charset.forName(charset);
		} catch (Exception e) {
			return null;
		}
	}
	
	private final String mimeType;
	private final Charset charset;
	private final Boolean compressible;

	public ResourceSettings(final String mimeType, final Charset charset, final Boolean compressible) {
		this.mimeType = mimeType;
		this.charset = charset;
		this.compressible = compressible;
	}
	
	ResourceSettings(final ResourceSettings base, final ResourceSettings override) {
		throw new AssertionError("not yet");
	}
	
	/**
	 * @return the mime type of the resource
	 */
	public String mimeType() {
		return mimeType;
	}
	
	/**
	 * @return the charset of the resource, null if this resource is not text
	 */
	public Charset charset() {
		return charset;
	}
	
	/**
	 * @return true if this resource has compressible contents
	 */
	public boolean compressible() {
		return compressible == null ? false : compressible;
	}
	
	@Override
	public String toString() {
		return String.format("%s(mimeType: %s, charset: %s, compressible: %s)",
			ResourceSettings.class.getSimpleName(),
			mimeType,
			charset,
			compressible
		);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(mimeType, charset, compressible);
	}

	/**
	 * @return the configured mime type, including the charset parameter if applicable
	 */
	public String contentType() {
		return mimeType() + (charset == null ? "" : "; charset=" + charset.name());
	}
}
