package jj.html;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

import net.jcip.annotations.ThreadSafe;

import org.jboss.netty.util.CharsetUtil;

@ThreadSafe
public class HTMLFragmentCache {

	private final ConcurrentHashMap<Path, HTMLFragment> cache;
	
	public HTMLFragmentCache() {
		cache = new ConcurrentHashMap<>();
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
		if (!cache.containsKey(location)) {
			HTMLFragment htmlFragment = constructFragment(location);
			if (htmlFragment != null) {
				cache.putIfAbsent(location, htmlFragment);
			}
		}
		return cache.get(location);
		
	}
	
	private HTMLFragment constructFragment(Path location) {
		try {
			return new HTMLFragment(new String(Files.readAllBytes(location), CharsetUtil.UTF_8));
		} catch (IOException e) {
			return null;
		}
	}
}
