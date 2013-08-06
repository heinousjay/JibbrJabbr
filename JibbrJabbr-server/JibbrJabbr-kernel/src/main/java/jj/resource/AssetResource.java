package jj.resource;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * jj internal assets
 * @author jason
 *
 */
@Singleton
public class AssetResource extends AbstractFileResource implements LoadedResource {
	
	public static final String JJ_JS = "jj.js";
	public static final String JQUERY_JS_DEV = "jquery-2.0.3.js";
	public static final String JQUERY_JS = "jquery-2.0.3.min.js";
	public static final String JQUERY_JS_MAP = "jquery-2.0.3.min.map";
	public static final String FAVICON_ICO = "favicon.ico";
	public static final String ERROR_404 = "errors/404.html";
	public static final Set<String> ASSETS;
	
	static {
		Set<String> assets = new HashSet<>();
		assets.add(JJ_JS);
		assets.add(JQUERY_JS_DEV);
		assets.add(JQUERY_JS);
		assets.add(JQUERY_JS_MAP);
		assets.add(FAVICON_ICO);
		assets.add(ERROR_404);
		ASSETS = Collections.unmodifiableSet(assets);
	}
	
	private final String mime;
	
	@Inject
	AssetResource(final ResourceCacheKey cacheKey, final Path basePath, final String baseName) throws IOException {
		super(cacheKey, baseName, basePath.resolve(baseName));
		mime = MimeTypes.get(baseName);
	}

	@Override
	public String mime() {
		return mime;
	}
	
	public ByteBuf bytes() {
		return Unpooled.wrappedBuffer(byteBuffer);
	}
}
