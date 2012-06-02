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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import jj.Blocking;
import jj.NonBlocking;

import net.jcip.annotations.Immutable;

import ch.qos.cal10n.MessageConveyorException;
import ch.qos.cal10n.util.AnnotationExtractor;

/**
 * simple sketch of a resource bundle for the message conveyor.
 * 
 * currently just as simple as it needs to be. we don't bother
 * with anything but a way to load up some bundles by an enum class and a locale.
 * 
 * Doesn't bother with the JDK resource bundle because this is meant to be immutable
 * and not terribly concerned with caching itself, that's some higher-level job.
 * 
 * We don't use the ch.qos.cal10n version because it can arbitrarily block while it reloads.
 * 
 * @author jason
 *
 */
@Immutable
class MessageBundle<E extends Enum<E>> {
	
	private static final String EXCEPTION_FORMAT = "Could not create a MessageBundle for %s with locale %s";

	private final Map<String, String> bundle;
	
	/**
	 * 
	 * @param enumClass
	 * @param locale
	 */
	@Blocking
	MessageBundle(final Class<E> bundleEnum, final Locale locale) {
		assert (bundleEnum != null) : "messageEnum is required";
		assert (locale != null) : "locale is required";
		
		try {
			bundle = generateBundle(bundleEnum, locale);
		} catch (Exception e) {
			throw new MessageConveyorException(String.format(EXCEPTION_FORMAT, bundleEnum, locale), e);
		}
	}
	
	@NonBlocking
	public String get(String key) {
		return bundle.get(key);
	}
	
	@NonBlocking
	public String get(E key) {
		return bundle.get(key.name());
	}
	
	@NonBlocking
	public Set<String> keySet() {
		return bundle.keySet();
	}
	
	@NonBlocking
	public int count() {
		return bundle.size();
	}
	
	private static final String FORMAT_L = "%s_%s.properties";
	private static final String FORMAT_LC = "%s_%s_%s.properties";
	private static final String FORMAT_LCV = "%s_%s_%s_%s.properties";
	
	private static void addAll(final Map<String, String> bundle, final InputStream stream, final Charset charset) throws IOException {
		Properties properties = new Properties();
		properties.load(new InputStreamReader(stream, charset));
		for (Object keyObj : properties.keySet()) {
			String key = (String)keyObj;
			bundle.put(key, properties.getProperty(key));
		}
	}
	
	private static Map<String, String> generateBundle(final Class<? extends Enum<?>> enumClass, final Locale locale) throws IOException {
		
		final String variant = locale.getVariant().toLowerCase();
		final String country = locale.getCountry().toLowerCase();
		final String language = locale.getLanguage().toLowerCase();
		
		final String baseName = AnnotationExtractor.getBaseName(enumClass);
		
		final String baseNamePath = baseName.replace('.', '/');
		HashMap<String, String> bundle = new HashMap<>();
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();

		final String candidateL = String.format(FORMAT_L, baseNamePath, language);
		final String charsetNameL = AnnotationExtractor.getCharset(enumClass, new Locale(language));
		final Charset charsetL = charsetNameL.isEmpty() ? UTF_8 : Charset.forName(charsetNameL);
		try (InputStream stream = classloader.getResourceAsStream(candidateL)) {
			if (stream != null) {
				addAll(bundle, stream, charsetL);
			}
		}

		if (!country.isEmpty()) {
			final String candidateLC = String.format(FORMAT_LC, baseNamePath, language, country);
			final String charsetNameLC = AnnotationExtractor.getCharset(enumClass, new Locale(language, country));
			final Charset charsetLC = charsetNameLC.isEmpty() ? UTF_8 : Charset.forName(charsetNameLC);
			try (InputStream stream = classloader.getResourceAsStream(candidateLC)) {
				if (stream != null) {
					addAll(bundle, stream, charsetLC);
				}
			}
			
			if (!variant.isEmpty()) {
				final String candidateLCV = String.format(FORMAT_LCV, baseNamePath, language, country, variant);
				final String charsetNameLCV = AnnotationExtractor.getCharset(enumClass, new Locale(language, country, variant));
				final Charset charsetLCV = charsetNameLCV.isEmpty() ? UTF_8 : Charset.forName(charsetNameLCV);
				try (InputStream stream = classloader.getResourceAsStream(candidateLCV)) {
					if (stream != null) {
						addAll(bundle, stream, charsetLCV);
					}
				}
			}
		}
		
		return Collections.unmodifiableMap(bundle);
	}

}
