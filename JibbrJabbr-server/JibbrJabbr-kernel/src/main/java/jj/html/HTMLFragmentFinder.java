package jj.html;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Instantiate a subclass of this class to get an HTMLFragment
 * instance by URI<pre>
 * new HTMLFragmentFinder(URI.create("file:/path/to/some.html")) {
 *   protected void htmlFragment(HTMLFragment htmlFragment) {
 *     // do something interesting with the fragment.
 *   }
 * }
 * 
 * 
 * might be adding a charset parameter - defaults to UTF8 though
 * 
 * </pre>
 * @author jason
 *
 */
public abstract class HTMLFragmentFinder {
	
	final URI uri;
	final Charset charset;
	
	public HTMLFragmentFinder(final URI uri) {
		this(uri, StandardCharsets.UTF_8);
	}
	
	public HTMLFragmentFinder(final URI uri, final Charset charset) {
		this.uri = uri;
		this.charset = charset;
		HTMLFragmentCache.offer(this);
	}
	
	protected abstract void htmlFragment(HTMLFragment htmlFragment);
	
	protected void failed(Throwable t) {}
}