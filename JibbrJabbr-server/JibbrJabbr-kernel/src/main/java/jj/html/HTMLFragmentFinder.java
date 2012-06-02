package jj.html;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

import jj.AsyncThreadPool;
import jj.Blocking;
import jj.NonBlocking;
import jj.SynchronousOperationCallback;
import jj.SynchThreadPool;

import net.jcip.annotations.ThreadSafe;

import org.jboss.netty.util.CharsetUtil;

@ThreadSafe
public class HTMLFragmentFinder {

	private final ConcurrentHashMap<Path, HTMLFragment> cache;
	private final SynchThreadPool synchExecutor;
	private final AsyncThreadPool asyncExecutor;
	
	@NonBlocking
	public HTMLFragmentFinder(
		SynchThreadPool synchExecutor, 
		AsyncThreadPool asyncExecutor
	) {
		
		assert (synchExecutor != null) : "SynchronousThreadPoolExecutor required";
		assert (asyncExecutor != null) : "AsynchronousThreadPoolExecutor required";
		
		this.synchExecutor = synchExecutor;
		this.asyncExecutor = asyncExecutor;
		cache = new ConcurrentHashMap<>();
	}
	
	/**
	 * Returns an HTMLFragment found at a particular location
	 * identified by a base Path and a url path segment.
	 * @param base
	 * @param url
	 * @return
	 */
	@Blocking
	public HTMLFragment find(final Path base, final String url) {
		assert (base != null) : "base parameter required";
		assert (url != null) : "url parameter required";
		return find(base.resolve(url));
	}
	
	/**
	 * Returns an HTML fragment identified by location, or null if it
	 * doesn't exist, can't be read... whatever, really.
	 * @param location
	 * @return
	 */
	@Blocking
	public HTMLFragment find(final Path location) {
		assert (location != null) : "location parameter required";
		if (!cache.containsKey(location)) {
			HTMLFragment htmlFragment = constructFragment(location);
			if (htmlFragment != null) {
				if (cache.putIfAbsent(location, htmlFragment) == null) {
					// this was a new one, register a callback with the file watch
					// service so we can keep updated
					
				}
			}
		}
		return cache.get(location);
	}
	
	@NonBlocking
	public void find(final Path base, final String url, final SynchronousOperationCallback<HTMLFragment> callback) {
		assert (base != null) : "base parameter required";
		assert (url != null) : "url parameter required";
		assert (callback != null) : "callback parameter required";
		find(base.resolve(url), callback);
	}
	
	@NonBlocking
	public void find(final Path location, final SynchronousOperationCallback<HTMLFragment> callback) {
		assert (location != null) : "location parameter required";
		assert (callback != null) : "callback parameter required";
		synchExecutor.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					callback.invokeComplete(asyncExecutor, find(location));
				} catch (Throwable t) {
					callback.invokeThrowable(asyncExecutor, t);
				}
			}
		});
	}
	
	@Blocking
	private HTMLFragment constructFragment(Path location) {
		try {
			return new HTMLFragment(new String(Files.readAllBytes(location), CharsetUtil.UTF_8));
		} catch (IOException e) {
			return null; 
			// there really isn't anything better to do here?
			// maybe we want to throw from here and let the caller deal with it,
			// but unchecked. time to make exceptions?
		}
	}
}
