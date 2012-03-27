package jj.api;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;


public class VersionTest {

	@Test
	public void testVersionClass() {
		// more or less if this doesn't throw exceptions
		// and these values aren't null, we're golden
		assertThat(Version.name, is(not(nullValue())));
		assertThat(Version.version, is(not(nullValue())));
	}
}
