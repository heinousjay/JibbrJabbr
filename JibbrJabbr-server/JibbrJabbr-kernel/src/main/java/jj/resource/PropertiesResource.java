package jj.resource;


import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.PropertyResourceBundle;

public class PropertiesResource extends AbstractFileResource {
	
	private final String uri;
	private PropertyResourceBundle properties;
	
	/**
	 * 
	 * @param baseUri the base URI of the server
	 * @param basePath the base path of the server
	 * @param path the path to the properties file
	 * @throws IOException
	 */
	PropertiesResource(
		final ResourceCacheKey cacheKey,
		final Path path,
		final String baseName
	) throws IOException {
		super(cacheKey, baseName, path);
		
		uri = sha1 + "/" + baseName;
		
		properties =
			new PropertyResourceBundle(new StringReader(byteBuffer.toString(UTF_8)));
	}
	
	public PropertyResourceBundle properties() {
		return properties;
	}
	
	@Override
	public String uri() {
		return uri;
	}

	@Override
	public String mime() {
		return "text/plain; charset=UTF-8";
	}

}
