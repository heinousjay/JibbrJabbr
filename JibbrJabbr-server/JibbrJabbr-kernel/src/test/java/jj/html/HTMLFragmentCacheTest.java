package jj.html;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jsoup.nodes.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HTMLFragmentCacheTest {

	private static final String FRAGMENT_HTML = "fragment.html";
	private static final String INDEX_HTML = "index.html";
	HTMLFragmentCache htmlFragmentCache;
	Path clamwhoresBase;
	
	@Before
	public void before() throws Exception {
		htmlFragmentCache = new HTMLFragmentCache();
		if (clamwhoresBase == null) {
			Path clamwhoresIndex = Paths.get(getClass().getResource("/com/clamwhores/index.html").toURI());
			clamwhoresBase = clamwhoresIndex.getParent();
		}
	}
	
	@After
	public void after() {
		htmlFragmentCache = null;
	}
	
	@Test
	public void testFindArgumentErrors() {
		try {
			htmlFragmentCache.find(null, null);
			fail("should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			
		}
		try {
			htmlFragmentCache.find(clamwhoresBase, null);
			fail("should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			
		}
		try {
			htmlFragmentCache.find(null, INDEX_HTML);
			fail("should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			
		}
	}
	
	@Test
	public void testReturnsNullForUnknownResource() {
		assertThat(htmlFragmentCache.find(clamwhoresBase, "nonsense"), is(nullValue()));
	}
	
	@Test
	public void testFindsHTMLFragment() {
		
		HTMLFragment index = htmlFragmentCache.find(clamwhoresBase, INDEX_HTML);
		assertThat(index, is(notNullValue()));
		assertThat(index.element(), is(instanceOf(Document.class)));
		
		HTMLFragment fragment = htmlFragmentCache.find(clamwhoresBase, FRAGMENT_HTML);
		assertThat(fragment, is(notNullValue()));
		assertThat(fragment.element(), is(not(instanceOf(Document.class))));
	}
	
	@Test 
	public void testCachesHTMLFragments() {
		HTMLFragment index1 = htmlFragmentCache.find(clamwhoresBase, INDEX_HTML);
		HTMLFragment index2 = htmlFragmentCache.find(clamwhoresBase, INDEX_HTML);
		HTMLFragment fragment1 = htmlFragmentCache.find(clamwhoresBase, FRAGMENT_HTML);
		HTMLFragment fragment2 = htmlFragmentCache.find(clamwhoresBase, FRAGMENT_HTML);
		assertThat(index1, is(sameInstance(index2)));
		assertThat(fragment1, is(sameInstance(fragment2)));
	}
	
	
}
