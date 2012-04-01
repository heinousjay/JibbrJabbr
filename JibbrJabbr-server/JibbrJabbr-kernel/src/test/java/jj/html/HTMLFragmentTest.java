package jj.html;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

public class HTMLFragmentTest {

	private static final String INDEX_HTML = "index.html";
	
	HTMLFragment index;
	Path clamwhoresBase;
	
	@Before
	public void before() throws URISyntaxException {
		if (clamwhoresBase == null) {
			Path clamwhoresIndex = Paths.get(getClass().getResource("/com/clamwhores/index.html").toURI());
			clamwhoresBase = clamwhoresIndex.getParent();
		}
		index = new HTMLFragmentCache().find(clamwhoresBase, INDEX_HTML);
	}
	
	@Test
	public void testReturnedElementsAreIndependent() {
		assertThat(index.element(), is(not(sameInstance(index.element()))));
	}
}
