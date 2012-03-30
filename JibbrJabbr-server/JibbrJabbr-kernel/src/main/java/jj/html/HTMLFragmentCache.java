package jj.html;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HTMLFragmentCache {

	public HTMLFragmentCache() {
		
	}
	
	/**
	 * Returns an HTMLFragment found at a particular location
	 * identified by a base Path and a url path segment
	 * @param base
	 * @param url
	 * @return
	 */
	public HTMLFragment find(Path base, String url) {
		if (base == null) throw new IllegalArgumentException("");
		if (url == null) throw new IllegalArgumentException("");
		return find(base.resolve(url));
	}
	
	public HTMLFragment find(Path location) {
		if (location == null) throw new IllegalArgumentException("");
		try {
			String source = new String(Files.readAllBytes(location), "UTF8");
			return new HTMLFragment(source);
		} catch (IOException e) {
			throw new IllegalArgumentException("", e);
		}
		
	}
}
