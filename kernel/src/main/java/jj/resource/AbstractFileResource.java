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

import jj.util.SHA1Helper;

/**
 * <p>
 * Provides base services for {@link FileResource} instances
 * 
 * @author jason
 *
 */
public abstract class AbstractFileResource extends AbstractResource implements FileResource {
	
	protected final Path path;
	protected final FileTime lastModified;
	protected final long size;
	protected final ByteBuf byteBuffer;
	
	private String sha1;
	
	@ResourceThread
	protected AbstractFileResource(
		final Dependencies dependencies,
		final Path path
	) {
		this(dependencies, path, true);
	}
	
	@ResourceThread
	protected AbstractFileResource(
		final Dependencies dependencies,
		final Path path,
		final boolean keepBytes
	) {
		super(dependencies);
		
		try {
		
			BasicFileAttributes attributes;
			
			try {
				attributes = Files.readAttributes(path, BasicFileAttributes.class);
			} catch (NoSuchFileException nsfe) {
				throw new NoSuchResourceException(getClass(), path);
			}
			
			if (!attributes.isRegularFile()) {
				throw new NoSuchResourceException(getClass(), path);
			}
			
			size = attributes.size();
			boolean large = size > dependencies.resourceConfiguration.maxFileSizeToLoad();
			
			if (large && keepBytes) {
				throw new ResourceNotViableException(path, 
					"resource is " + size + " bytes but configured maximum is " + 
					dependencies.resourceConfiguration.maxFileSizeToLoad() + " bytes"
				);
			}
			
			this.path = path;
			this.lastModified = attributes.lastModifiedTime();
			
			if (keepBytes) { // read it all in
				byteBuffer = readAllBytes(path);
				sha1 = SHA1Helper.keyFor(byteBuffer);
			// read the SHA-1 directly if the size is under the configured limit
			// or we're getting it from a jar
			} else if (!large || path.getFileSystem() != FileSystems.getDefault()) {
				byteBuffer = null;
				sha1 = SHA1Helper.keyFor(path);
			} else { // avoid reading the sha1 directly, try to save it
				byteBuffer = null;
				Sha1Resource sha1Resource = resourceFinder.loadResource(Sha1Resource.class, base, name, this);
				assert sha1Resource.representedFileSize() == size;
				sha1 = sha1Resource.representedSha();
			}
			
		} catch (IOException ioe) {
			throw new ResourceNotViableException(path, ioe);
		}
	}
	
	private ByteBuf readAllBytes(final Path path) throws IOException {
		return Unpooled.wrappedBuffer(Files.readAllBytes(path));
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
	public boolean isDirectory() {
		return false;
	}

	@Override
	public long size() {
		return size;
	}
	
	@Override
	@ResourceThread
	public boolean needsReplacing() throws IOException {
		return (path.getFileSystem() == FileSystems.getDefault()) && lastModified.compareTo(Files.getLastModifiedTime(path)) < 0;
	}

	public FileTime lastModified() {
		return lastModified;
	}
}