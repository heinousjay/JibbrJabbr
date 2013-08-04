package jj.resource;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.file.Path;

/**
 * jj internal assets
 * @author jason
 *
 */
public class AssetResource extends AbstractFileResource implements LoadedResource {
	
	public static final String JQUERY_JS = "jquery-2.0.3.min.js";
	public static final String JJ_JS = "jj.js";
	public static final String FAVICON_ICO = "favicon.ico";
	private final String mime;
	
	AssetResource(final ResourceCacheKey cacheKey, final Path basePath, final String baseName) throws IOException {
		super(cacheKey, baseName, basePath.resolve(baseName));
		mime = MimeTypes.get(baseName);
	}

	@Override
	public String uri() {
		return "/" + sha1 + "/" + baseName;
	}

	@Override
	public String mime() {
		return mime;
	}
	
	public ByteBuf bytes() {
		return Unpooled.wrappedBuffer(byteBuffer);
	}
}
