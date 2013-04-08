package jj.resource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

/**
 * jj internal assets
 * @author jason
 *
 */
public class AssetResource extends AbstractFileResource {
	
	private final String absoluteUri;
	private final String mime;
	
	AssetResource(final Path basePath, final URI baseUri, final String baseName) throws IOException {
		super(baseName, basePath.resolve(baseName));
		absoluteUri = baseUri.toString() + uri();
		mime = MimeTypes.get(baseName);
	}

	@Override
	public String uri() {
		return "/" + sha1 + "/" + baseName;
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
