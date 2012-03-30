package jj.html;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.hamcrest.collection.IsEmptyCollection;
import org.jsoup.nodes.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HTMLFragmentCacheTest {

	HTMLFragmentCache underTest;
	Path clamwhoresBase;
	
	@Before
	public void before() throws Exception {
		underTest = new HTMLFragmentCache();
		Path clamwhoresIndex = Paths.get(getClass().getResource("/com/clamwhores/index.html").toURI());
		clamwhoresBase = clamwhoresIndex.getParent();
	}
	
	@After
	public void after() {
		underTest = null;
	}
	
	@Test
	public void testFindArgumentErrors() {
		try {
			underTest.find(null, null);
			fail("should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			
		}
		try {
			underTest.find(clamwhoresBase, null);
			fail("should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			
		}
		try {
			underTest.find(null, "index.html");
			fail("should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			
		}
	}
	
	@Test
	public void testFindsHTMLFragment() {
		
		HTMLFragment index = underTest.find(clamwhoresBase, "index.html");
		assertThat(index, is(notNullValue()));
		assertThat(index.element(), is(instanceOf(Document.class)));
		//assertThat(index.errors(), hasSize(0));
		System.out.print(index.element());
	}
	
	
}
