package jj.resource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * jj internal assets
 * @author jason
 *
 */
public class AssetResource extends AbstractResource {
	
	private static final Map<String, String> mimeTypes;
	
	static {
		
		Map<String, String> mimeTypesMaker = new HashMap<String, String>();
		mimeTypesMaker.put("html", "text/html; charset=UTF-8");
		mimeTypesMaker.put("js", "application/javascript; charset=UTF-8");
		mimeTypesMaker.put("css", "text/css; charset=UTF-8");
		mimeTypes = Collections.unmodifiableMap(mimeTypesMaker);
	}
	
	private final String absoluteUri;
	private final String mime;
	
	AssetResource(final Path basePath, final URI baseUri, final String baseName) throws IOException {
		super(baseName, basePath.resolve(baseName));
		absoluteUri = baseUri.toString() + baseName;
		String extension = baseName.substring(baseName.lastIndexOf('.') + 1);
		mime = mimeTypes.get(extension);
	}

	@Override
	public String uri() {
		return "/" + baseName;
	}

	@Override
	public String absoluteUri() {
		return absoluteUri;
	}

	@Override
	public String mime() {
		return mime;
	}
	
	public byte[] bytes() {
		return bytes;
	}

}
