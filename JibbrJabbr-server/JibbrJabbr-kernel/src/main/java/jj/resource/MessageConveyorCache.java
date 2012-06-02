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

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import jj.AsyncThreadPool;
import jj.Blocking;
import jj.NonBlocking;
import jj.SynchThreadPool;
import jj.SynchronousOperationCallback;
import net.jcip.annotations.ThreadSafe;

/**
 * Caches instances of MessageConveyor objects keyed by the
 * enumeration and locale
 * 
 * @author jason
 *
 */
@ThreadSafe
public class MessageConveyorCache {
	
	private static final class Key {
		
		private final Class<?> klass;
		private final Locale locale;
		
		Key(final Class<?> klass, final Locale locale) {
			assert(klass != null) : "klass is required";
			assert(locale != null) : "locale is required";
			this.klass = klass;
			this.locale = locale;
		}
		
		@Override
		public int hashCode() {
			return klass.hashCode() ^ locale.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			return (obj != null) && (obj instanceof Key) &&
				((Key)obj).klass == klass &&
				((Key)obj).locale.equals(locale);
		}
	}
	
	private final ConcurrentHashMap<Key, MessageConveyor<?>> store;
	
	private final AsyncThreadPool asyncThreadPool;
	
	private final SynchThreadPool synchThreadPool;

	@NonBlocking
	public MessageConveyorCache(final AsyncThreadPool asyncThreadPool, final SynchThreadPool synchThreadPool) {
		assert (asyncThreadPool != null) : "asyncThreadPool is required";
		assert (synchThreadPool != null) : "synchThreadPool is required";
		
		this.asyncThreadPool = asyncThreadPool;
		this.synchThreadPool = synchThreadPool;
		store = new ConcurrentHashMap<>();
	}
	
	/**
	 * Retrieves a {@link MessageConveyor} instance for a given {@link Enum} class and {@link Locale},
	 * potentially blocking to create it
	 * @param bundleEnum The {@link Enum} that defines the message bundle.
	 * @param locale The {@link Locale} of the bundle to retrieve
	 * @return 
	 */
	@Blocking
	public <E extends Enum<E>> MessageConveyor<E> getMessageConveyor(final Class<E> bundleEnum, final Locale locale) {
		
		assert(bundleEnum != null) : "bundleEnum is required";
		assert(locale != null) : "locale is required";
		
		Key key = new Key(bundleEnum, locale);
		if (!store.containsKey(key)) {
			store.putIfAbsent(key, new PreloadingMessageConveyor<>(bundleEnum, locale));
		}
		
		// specifically bothering with this technique to keep the @SuppressWarnings scope small
		@SuppressWarnings("unchecked")
		MessageConveyor<E> result = (MessageConveyor<E>)store.get(key);
		
		return result;
	}
	
	/**
	 * Non-blocking implementation of {@link #getMessageConveyor(Class, Locale)}
	 * @param bundleEnum The {@link Enum} that defines the message bundle.
	 * @param locale The {@link Locale} of the bundle to retrieve
	 * @param callback
	 */
	@NonBlocking
	public <E extends Enum<E>> void getMessageConveyor(
		final Class<E> bundleEnum,
		final Locale locale,
		final SynchronousOperationCallback<MessageConveyor<E>> callback
	) {
		assert(bundleEnum != null) : "bundleEnum is required";
		assert(locale != null) : "locale is required";
		assert(callback != null) : "callback is required";
		
		synchThreadPool.submit(new Runnable() {
			
			@Override
			public void run() {
				try {
					callback.invokeComplete(asyncThreadPool, getMessageConveyor(bundleEnum, locale));
				} catch (Throwable t) {
					callback.invokeThrowable(asyncThreadPool, t);
				}
			}
		});
	}
	
}
