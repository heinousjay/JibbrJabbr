package jj.resource;


import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.PropertyResourceBundle;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PropertiesResource extends AbstractFileResource {
	
	private PropertyResourceBundle properties;
	
	/**
	 * 
	 * @param baseUri the base URI of the server
	 * @param appPath the base path of the server
	 * @param path the path to the properties file
	 * @throws IOException
	 */
	@Inject
	PropertiesResource(
		final ResourceCacheKey cacheKey,
		final Path path,
		final String baseName
	) throws IOException {
		super(cacheKey, baseName, path);
		
		properties =
			new PropertyResourceBundle(new StringReader(byteBuffer.toString(UTF_8)));
	}
	
	public PropertyResourceBundle properties() {
		return properties;
	}
	
	@Override
	public String mime() {
		return "text/plain; charset=UTF-8";
	}

}
