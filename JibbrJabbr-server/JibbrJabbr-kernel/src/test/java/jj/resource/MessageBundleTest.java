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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

/**
 * @author jason
 *
 */
public class MessageBundleTest {
	
	@BaseName("jj.resource.test")
	@LocaleData(value = {
		@Locale("en"),
		@Locale("fr")
	}, defaultCharset="UTF-8")
	public static enum TestKeys {
		TestMessage1,
		TestMessage2,
		TestMessage3
	}

	@Test
	public void testConstructionByLocale() {
		MessageBundle<TestKeys> mb = new MessageBundle<>(TestKeys.class, java.util.Locale.US);
		assertThat(mb, is(notNullValue()));
		assertThat(mb.get(TestKeys.TestMessage1), is("test message"));
		assertThat(mb.get("TestMessage1"), is("test message"));
		assertThat(mb.get(TestKeys.TestMessage2), is("test message 2"));
		assertThat(mb.get("TestMessage2"), is("test message 2"));
		assertThat(mb.get(TestKeys.TestMessage3), is("Jåsøñ Mîllé®"));
		assertThat(mb.get("TestMessage3"), is("Jåsøñ Mîllé®"));
		
		MessageBundle<TestKeys> mb2 = new MessageBundle<>(TestKeys.class, java.util.Locale.FRENCH);
		assertThat(mb2, is(notNullValue()));
		assertThat(mb2.get(TestKeys.TestMessage1), is("french message! Oui!"));
		assertThat(mb2.get("TestMessage1"), is("french message! Oui!"));
		assertThat(mb2.get(TestKeys.TestMessage2), is("fake french!"));
		assertThat(mb2.get("TestMessage2"), is("fake french!"));
		assertThat(mb2.get(TestKeys.TestMessage3), is("Jåsøñ Mîllé®"));
		assertThat(mb2.get("TestMessage3"), is("Jåsøñ Mîllé®"));
		
		MessageBundle<TestKeys> mb3 = new MessageBundle<>(TestKeys.class, java.util.Locale.CANADA_FRENCH);
		assertThat(mb3, is(notNullValue()));
		assertThat(mb3.get(TestKeys.TestMessage1), is("french message! Oui!"));
		assertThat(mb3.get("TestMessage1"), is("french message! Oui!"));
		assertThat(mb3.get(TestKeys.TestMessage2), is("fake french!"));
		assertThat(mb3.get("TestMessage2"), is("fake french!"));
		assertThat(mb3.get(TestKeys.TestMessage3), is("Jåsøñ Mîllé® is a hoseur"));
		assertThat(mb3.get("TestMessage3"), is("Jåsøñ Mîllé® is a hoseur"));
	}
	
	@Test
	public void testEquivalence() {
		
		MessageBundle<TestKeys> mb = new MessageBundle<>(TestKeys.class, java.util.Locale.US);
		for (TestKeys key : TestKeys.values()) {
			assertThat(mb.get(key), is(mb.get(key.name())));
		}
	}
	
}
