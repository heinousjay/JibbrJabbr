package jj;

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
		assertThat(Version.branchName, is(not(nullValue())));
		assertThat(Version.commitId, is(not(nullValue())));
		assertThat(Version.commitDescription, is(not(nullValue())));
		assertThat(Version.commitUserName, is(not(nullValue())));
		assertThat(Version.commitUserEmail, is(not(nullValue())));
		assertThat(Version.commitDate, is(not(nullValue())));
		assertThat(Version.buildDate, is(not(nullValue())));
	}
}
