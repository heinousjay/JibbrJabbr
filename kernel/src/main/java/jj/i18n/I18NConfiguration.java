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
package jj.i18n;

import java.util.Locale;

/**
 * 
 * @author jason
 *
 */
public interface I18NConfiguration {

	/**
	 * <p>
	 * Indicates if the server should accept country and language codes that are not
	 * found in the lists returned from {@link Locale#getISOCountries()} and
	 * {@link Locale#getISOLanguages()}
	 * 
	 * <p>
	 * defaults to false, meaning only ISO codes will be accepted.
	 */
	boolean allowNonISO();
	
	/**
	 * <p>
	 * The default Locale to be served.  Defaults to the locale returned by
	 * {@link Locale#getDefault()}.  This is the Locale used when no better
	 * alternative can be negotiated.
	 */
	Locale defaultLocale();
}
