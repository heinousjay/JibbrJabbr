package jj.resource;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import jj.SHA1Helper;
import jj.execution.IOThread;

// needs to be public or mockito can't mock this, so the constructor
// is package protected to prevent outside things from deriving from
// this.  for now at least.
/**
 * re
 * @author jason
 *
 */
public abstract class AbstractFileResource extends AbstractResourceBase {
	
	// beyond this, we don't keep bytes
	private static final long MAX_IN_MEMORY_SIZE  = 1000000;
	// beyond this, we don't read the SHA inside here
	private static final long MAX_READ_AND_DIGEST = 10000000;
	
	private static final String TOO_LARGE_ASSERTION_ERROR =
		AbstractFileResource.class.getSimpleName() + " asked to load a file over " + MAX_IN_MEMORY_SIZE + " bytes";

	protected static final Object[] EMPTY_ARGS = {};
	
	protected final String baseName;
	protected final Path path;
	protected final FileTime lastModified;
	protected final long size;
	protected final ByteBuf byteBuffer;
	
	private String toString;
	private String sha1;
	
	@IOThread
	protected
	AbstractFileResource(
		final ResourceCacheKey cacheKey,
		final String baseName,
		final Path path
	) {
		this(cacheKey, baseName, path, true);
	}
	
	@IOThread
	protected
	AbstractFileResource(
		final ResourceCacheKey cacheKey,
		final String baseName,
		final Path path,
		final boolean keepBytes
	) {
		super(cacheKey);
		
		try {
		
			BasicFileAttributes attributes;
			
			try {
				attributes = Files.readAttributes(path, BasicFileAttributes.class);
			} catch (NoSuchFileException nsfe) {
				throw new NoSuchResourceException(path);
			}
			
			if (!attributes.isRegularFile()) {
				throw new NoSuchResourceException(path);
			}
			
			size = attributes.size();
			boolean large = size > MAX_IN_MEMORY_SIZE;
			
			assert !(large && keepBytes) : TOO_LARGE_ASSERTION_ERROR;
			
			this.baseName = baseName;
			this.path = path;
			this.lastModified = attributes.lastModifiedTime();
			
			if (keepBytes) {
				byteBuffer = readAllBytes(path);
				sha1 = SHA1Helper.keyFor(byteBuffer);
				toString = getClass().getSimpleName() + ":" + sha1 + " at " + path;
			} else if (size <= MAX_READ_AND_DIGEST) {
				byteBuffer = null;
				sha1 = SHA1Helper.keyFor(path);
				toString = getClass().getSimpleName() + ":" + sha1 + " at " + path;
			} else {
				byteBuffer = null;
				sha1 = null;
				toString =  getClass().getSimpleName() + " (large) at " + path;
			}
			
		} catch (IOException ioe) {
			throw new ResourceNotViableException(path, ioe);
		}
	}
	
	private ByteBuf readAllBytes(final Path path) throws IOException {
		return Unpooled.wrappedBuffer(Files.readAllBytes(path));
	}
	
	@Override
	public String baseName() {
		return baseName;
	}

	@Override
	public String uri() {
		String sha = sha1();
		StringBuilder sb = new StringBuilder("/");
		if (sha != null) {
			sb.append(sha).append("/");
		}
		
		return sb.append(baseName).toString();
	}

	@Override
	public String sha1() {
		return sha1;
	}

	@Override
	public Path path() {
		return path;
	}

	@Override
	public FileTime lastModified() {
		return lastModified;
	}

	@Override
	public long size() {
		return size;
	}
	
	@Override
	public Date lastModifiedDate() {
		return new Date(lastModified.toMillis());
	}
	
	@Override
	@IOThread
	protected boolean needsReplacing() throws IOException {
		return (path.getFileSystem() == FileSystems.getDefault()) && lastModified.compareTo(Files.getLastModifiedTime(path)) < 0;
	}
	
	@Override
	protected Object[] creationArgs() {
		return EMPTY_ARGS;
	}
	
	@Override
	public String toString() {
		return toString;
	}
}