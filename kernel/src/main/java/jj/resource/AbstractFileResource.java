package jj.resource;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import jj.util.SHA1Helper;

/**
 * <p>
 * Provides base services for {@link FileResource} instances.
 * 
 * @author jason
 *
 */
public abstract class AbstractFileResource<T> extends AbstractResource<T> implements FileResource<T> {
	
	protected final Path path;
	protected final FileTime lastModified;
	protected final long size;
	protected final ByteBuf byteBuffer;
	protected final String sha1;
	
	@ResourceThread
	protected AbstractFileResource(
		final Dependencies dependencies,
		final Path path
	) {
		this(dependencies, path, true);
	}
	
	// java likes to be a pain
	@SuppressWarnings("unchecked")
	private static NoSuchResourceException noSuchResourceException(Class<?> instanceClass, Path path) {
		throw new NoSuchResourceException((Class<? extends Resource<?>>)instanceClass, path);
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
				throw noSuchResourceException(getClass(), path);
			}
			
			if (!attributes.isRegularFile()) {
				throw noSuchResourceException(getClass(), path);
			}
			
			size = attributes.size();
			boolean large = size > resourceConfiguration.maxFileSizeToLoad();
			
			if (large && keepBytes) {
				throw new ResourceNotViableException(path, 
					"resource is " + size + " bytes but configured maximum is " + 
					resourceConfiguration.maxFileSizeToLoad() + " bytes"
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
				Sha1Resource sha1Resource = resourceFinder.loadResource(Sha1Resource.class, base(), name(), new Sha1ResourceTarget(this));
				assert sha1Resource.representedFileSize() == size;
				sha1 = sha1Resource.representedSha();
			}
			
		} catch (IOException ioe) {
			throw new ResourceNotViableException(path, ioe);
		}
	}
	
	@Override
	protected String extension() {
		// file resources get a path based on their name, right?
		// at least in all cases where these settings matter, anyway
		String name = name();
		return name.substring(name.lastIndexOf(".") + 1);
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
	public Charset charset() {
		return settings.charset();
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