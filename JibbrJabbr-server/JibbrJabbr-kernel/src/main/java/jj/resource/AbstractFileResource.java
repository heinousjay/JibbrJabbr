package jj.resource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Date;

import jj.IOThread;
import jj.SHA1Helper;

// needs to be public or mockito can't mock this, so the constructor
// is package protected to prevent outside things from deriving from
// this.  for now at least.
/**
 * re
 * @author jason
 *
 */
public abstract class AbstractFileResource implements Resource {
	
	// for now, a hard line in the sand.  webbit doesn't much like large files,
	// so even this number might be optimistic
	private static final long MAX_FILE_SIZE = 1000000;

	private static final Object[] EMPTY_ARGS = {};
	
	protected final String baseName;
	protected final String sha1;
	protected final Path path;
	protected final FileTime lastModified;
	protected final ByteBuffer byteBuffer;
	
	private final String toString;
	
	@IOThread
	AbstractFileResource(
		final String baseName,
		final Path path
	) throws IOException {
		this(baseName, path, true);
	}
	
	@IOThread
	AbstractFileResource(
		final String baseName,
		final Path path,
		final boolean keepBytes
	) throws IOException {
		
		if (Files.size(path) > MAX_FILE_SIZE) {
			throw new IOException(AbstractFileResource.class.getSimpleName() + " asked to load a file over " + MAX_FILE_SIZE + " bytes");
		}
		
		this.baseName = baseName;
		this.path = path;
		this.lastModified = Files.getLastModifiedTime(this.path);
		
		ByteBuffer bytes = readAllBytes(path);
		sha1 = SHA1Helper.keyFor(bytes);
		toString = getClass().getSimpleName() + ":" + sha1 + " at " + path;
		
		byteBuffer = keepBytes ? bytes : null;
	}
	
	private ByteBuffer readAllBytes(final Path path) throws IOException {
		return ByteBuffer.wrap(Files.readAllBytes(path));
	}
	
	@Override
	public Object[] creationArgs() {
		return EMPTY_ARGS;
	}
	
	@Override
	public String baseName() {
		return baseName;
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
	public Date lastModifiedDate() {
		return new Date(lastModified.toMillis());
	}
	
	@IOThread
	public boolean needsReplacing() throws IOException {
		return lastModified.compareTo(Files.getLastModifiedTime(path)) < 0;
	}
	
	public abstract String mime();
	
	@Override
	public final String toString() {
		return toString;
	}
}