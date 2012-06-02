package jj.html;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.netty.util.CharsetUtil;
import org.junit.Before;
import org.junit.Test;

public class HTMLFragmentTest {

	private static final String CLAMWHORES_INDEX_HTML = "/com/clamwhores/index.html";
	
	HTMLFragment index;
	Path clamwhoresIndex;
	
	@Before
	public void before() throws Exception {
		if (clamwhoresIndex == null) {
			clamwhoresIndex = Paths.get(getClass().getResource(CLAMWHORES_INDEX_HTML).toURI());
		}
		index = new HTMLFragment(new String(Files.readAllBytes(clamwhoresIndex), CharsetUtil.UTF_8));
	}
	
	@Test
	public void testReturnedElementsAreIndependent() {
		assertThat(index.element(), is(not(sameInstance(index.element()))));
	}
}
