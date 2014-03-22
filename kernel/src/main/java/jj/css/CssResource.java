package jj.css;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.AppLocation;
import jj.resource.AbstractResource;
import jj.resource.FileResource;
import jj.resource.ResourceThread;
import jj.resource.LoadedResource;
import jj.resource.MimeTypes;
import jj.resource.ResourceCacheKey;
import jj.util.SHA1Helper;

/**
 * <p>
 * represents a css file, optionally loaded by processing a less file
 * </p>
 * 
 * <p>
 * takes a single argument, true if this should be a less file, false or empty if
 * a regular stylesheet.
 * </p>
 * 
 * @author jason
 *
 */
@Singleton
public class CssResource extends AbstractResource implements FileResource, LoadedResource {
	
	protected final String baseName;
	protected final Path path;
	private final boolean less;
	protected final FileTime lastModified;
	protected final ByteBuf byteBuffer;
	private String sha1;
	private String uri;
	private String toString;
	
	@Inject
	CssResource(final ResourceCacheKey cacheKey, final AppLocation base, final String name, final Path path, final boolean less) throws IOException {
		super(cacheKey, base);
		this.baseName = name;
		this.path = path;
		this.less = less;
		this.lastModified = Files.getLastModifiedTime(this.path);
		this.byteBuffer = Unpooled.buffer((int)Files.size(this.path));
		if (!less) {
			this.byteBuffer.writeBytes(Files.readAllBytes(this.path));
			this.sha1 = SHA1Helper.keyFor(this.byteBuffer);
		}
	}

	@Override
	public String uri() {
		return uri;
	}

	@Override
	public String mime() {
		return MimeTypes.get(".css");
	}
	
	@Override
	public ByteBuf bytes() {
		return Unpooled.wrappedBuffer(byteBuffer);
	}

	@Override
	public String name() {
		return baseName;
	}
	
	private static final Object[] LESS_ARG = new Object[] {Boolean.TRUE};

	@Override
	public Path path() {
		return path;
	}

	@Override
	public long size() {
		return byteBuffer.readableBytes();
	}

	@Override
	public String sha1() {
		return sha1;
	}
	
	@Override
	public String toString() {
		return toString;
	}
	
	CssResource sha1(String sha1) {
		this.sha1 = sha1;
		toString = getClass().getSimpleName() + ":" + sha1 + " at " + path;
		return this;
	}
	
	CssResource uri(String uri) {
		this.uri = "/" + sha1 + "/" + uri;
		return this;
	}
	
	@ResourceThread
	public boolean needsReplacing() throws IOException {
		return lastModified.compareTo(Files.getLastModifiedTime(path)) < 0;
	}

	@Override
	protected Object[] creationArgs() {
		return less ? LESS_ARG : null;
	}
	
	@Override
	protected boolean removeOnReload() {
		// we are (for now) the root of the css system.  i feel like things
		// will change a bit in here
		return false;
	}
}
