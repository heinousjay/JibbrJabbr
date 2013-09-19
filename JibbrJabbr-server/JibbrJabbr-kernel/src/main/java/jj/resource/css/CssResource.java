package jj.resource.css;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.SHA1Helper;
import jj.execution.IOThread;
import jj.resource.AbstractResourceBase;
import jj.resource.FileResource;
import jj.resource.LoadedResource;
import jj.resource.MimeTypes;
import jj.resource.ResourceCacheKey;

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
public class CssResource extends AbstractResourceBase implements FileResource, LoadedResource {
	
	protected final String baseName;
	protected final Path path;
	private final boolean less;
	protected final FileTime lastModified;
	protected final ByteBuf byteBuffer;
	private String sha1;
	private String uri;
	private String toString;
	
	@Inject
	CssResource(final ResourceCacheKey cacheKey, final String baseName, final Path path, final boolean less) throws IOException {
		super(cacheKey);
		this.baseName = baseName;
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
	public String baseName() {
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
		uri = "/" + sha1 + "/" + baseName;
		toString = getClass().getSimpleName() + ":" + sha1 + " at " + path;
		return this;
	}
	
	@IOThread
	protected boolean needsReplacing() throws IOException {
		return lastModified.compareTo(Files.getLastModifiedTime(path)) < 0;
	}

	@Override
	protected Object[] creationArgs() {
		return less ? LESS_ARG : null;
	}
}