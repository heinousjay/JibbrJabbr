package jj.resource;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Date;

import jj.IOThread;
import jj.SHA1Helper;

public class CssResource implements LoadedResource {
	
	protected final String baseName;
	protected final Path path;
	private final boolean less;
	protected final FileTime lastModified;
	protected final ByteBuf byteBuffer;
	protected long size;
	protected String sha1;
	
	CssResource(final String baseName, final Path path, final boolean less) throws IOException {
		this.baseName = baseName;
		this.path = path;
		this.less = less;
		this.size = Files.size(this.path);
		this.lastModified = Files.getLastModifiedTime(this.path);
		this.byteBuffer = Unpooled.buffer((int)this.size);
		if (!less) {
			this.byteBuffer.writeBytes(Files.readAllBytes(this.path));
			this.sha1 = SHA1Helper.keyFor(this.byteBuffer);
		}
	}

	@Override
	public String uri() {
		return sha1() + "/" + baseName;
	}

	@Override
	public String mime() {
		return "text/css; charset=UTF-8";
	}
	
	@Override
	public ByteBuf bytes() {
		return byteBuffer;
	}

	@Override
	public String baseName() {
		return baseName;
	}
	
	private static final Object[] LESS_ARG = new Object[] {Boolean.TRUE};

	@Override
	public Object[] creationArgs() {
		return less ? LESS_ARG : null;
	}

	@Override
	public Path path() {
		return path;
	}

	@Override
	public long size() {
		return byteBuffer.readableBytes();
	}
	
	@Override
	public Date lastModifiedDate() {
		return new Date(lastModified.toMillis());
	}
	
	@IOThread
	public boolean needsReplacing() throws IOException {
		return lastModified.compareTo(Files.getLastModifiedTime(path)) < 0;
	}

	@Override
	public FileTime lastModified() {
		return lastModified;
	}

	@Override
	public String sha1() {
		return sha1;
	}
}
