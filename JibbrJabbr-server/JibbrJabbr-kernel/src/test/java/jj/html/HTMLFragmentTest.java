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

	private static final String INDEX_HTML = "/jj/html/index.html";
	
	HTMLFragment index;
	Path indexPath;
	
	@Before
	public void before() throws Exception {
		if (indexPath == null) {
			indexPath = Paths.get(getClass().getResource(INDEX_HTML).toURI());
		}
		index = new HTMLFragment(new String(Files.readAllBytes(indexPath), CharsetUtil.UTF_8));
	}
	
	@Test
	public void testReturnedElementsAreIndependent() {
		assertThat(index.element(), is(not(sameInstance(index.element()))));
	}
}
