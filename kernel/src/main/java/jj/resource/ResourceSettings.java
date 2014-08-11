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
	
	public String mimeType() {
		return mimeType;
	}
	
	public Charset charset() {
		return charset;
	}
	
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
}
