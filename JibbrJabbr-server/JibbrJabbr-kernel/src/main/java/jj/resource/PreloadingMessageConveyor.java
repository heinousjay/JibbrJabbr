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

import java.text.MessageFormat;
import java.util.Locale;

import net.jcip.annotations.Immutable;

import jj.api.Blocking;
import jj.api.NonBlocking;

/**
 * Custom implementation of the IMessageConveyor interface from cal10n,
 * mainly to elide the reloading behavior of the provided implementation.
 * 
 * basically only for use in the kernel, for now
 * 
 * this implementation locates its resource bundle upon creation and that
 * is that.
 * 
 * @author jason
 *
 */
@Immutable
public class PreloadingMessageConveyor<E extends Enum<E>> implements MessageConveyor<E> {
	
	private final MessageBundle<E> messageBundle;
	
	@Blocking
	public PreloadingMessageConveyor(final Class<E> bundleEnum, final Locale locale) {
		this.messageBundle = new MessageBundle<>(bundleEnum, locale);
	}
	
	@Override
	@NonBlocking
	public String getMessage(final E key, final Object... args) {
		
		String result = null;
		String pattern = messageBundle.get(key);
		if (pattern != null) {
			result = MessageFormat.format(pattern, args);
		}
		
		return result;
	}
	
	@NonBlocking
	public int count() {
		return messageBundle.count();
	}

}
