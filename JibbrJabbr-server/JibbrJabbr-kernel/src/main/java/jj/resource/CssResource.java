package jj.resource;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.file.Path;

public class CssResource extends AbstractFileResource implements LoadedResource {
	
	CssResource(final String baseName, final Path path, final boolean less) throws IOException {
		super(baseName, path, !less);
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
		return lessBytes != null ? lessBytes : byteBuffer;
	}
	
	ByteBuf lessBytes;
}
