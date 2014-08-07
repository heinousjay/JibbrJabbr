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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.netty.handler.codec.http.HttpHeaders;

/**
 * <p>
 * Parses the accept-lang header from a given headers set,
 * and makes the contents available as a {@link List} of 
 * {@link Locale}s in order of preference as determined by
 * q-values
 * 
 * <p>
 * The algorithm is kinda wasteful of memory.  oh well
 * 
 * @author jason
 *
 */
class AcceptLangHeaderReader {
	
	private static final class SortableLocale implements Comparable<SortableLocale> {

		final Locale locale;
		final BigDecimal qValue;
		
		SortableLocale(final Locale locale, final String qValue) {
			this.locale = locale;
			this.qValue = new BigDecimal(qValue);
		}
		
		@Override
		public int compareTo(SortableLocale o) {
			// sort them in reverse!
			return o.qValue.compareTo(qValue);
		}
		
	}
	
	private static final Pattern LIST_SPLITTER = Pattern.compile("\\s*,\\s*");
	private static final Pattern VALUE_PARSER = 
		// this pattern is kinda complicated to read, it is meant to match the grammar specified at
		// http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.4
		// group 1 is the lang
		// group 2 is the country, if any
		// group 3 is the q-value, if any
		Pattern.compile("^(?:([a-zA-Z]{1,8}(?:-[a-zA-Z]{1,8})?)|\\*)(?:;q=((?:0\\.\\d{1,3})|1|0))?$");
	
	private final List<Locale> locales;
	
	private boolean badRequest = false;

	// need to get a hold of the server default locale somewhere?
	AcceptLangHeaderReader(final HttpHeaders requestHeaders) {
		
		String headerValue = requestHeaders.get(HttpHeaders.Names.ACCEPT_LANGUAGE);
		
		locales = parseLocales(LIST_SPLITTER.split(headerValue));
	}
	
	private List<Locale> parseLocales(String[] incomingValues) {
		ArrayList<SortableLocale> holder = new ArrayList<>(incomingValues.length);
		
		for (String value : incomingValues) {
			Matcher matcher = VALUE_PARSER.matcher(value);
			if (!matcher.matches()) { // it either matches or we hates you.  HATES
				badRequest = true;
				break;
			}
			String languageTag = matcher.group(1);
			String qValue = matcher.group(2);
			if (qValue == null) qValue = "1";
			Locale locale = Locale.forLanguageTag(languageTag);
			
			if (!"0".equals(qValue)) {
				// "0" means don't use... which really we can't guarantee,
				// if, for instance, it's the only Locale we support
				holder.add(new SortableLocale(locale, qValue));
			}
		}
		
		Collections.sort(holder);
		
		ArrayList<Locale> result = new ArrayList<>(holder.size());
		for (SortableLocale sortableLocale : holder) {
			result.add(sortableLocale.locale);
		}
		
		return Collections.unmodifiableList(result);
	}

	public List<Locale> locales() {
		return locales;
	}
	
	public boolean isBadRequest() {
		return badRequest;
	}
	
}
