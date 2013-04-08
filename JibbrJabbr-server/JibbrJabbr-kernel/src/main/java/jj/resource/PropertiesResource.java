package jj.resource;


import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.PropertyResourceBundle;

public class PropertiesResource extends AbstractFileResource {

	private static final String DOT_PROPERTIES = ".properties";
	
	private final String uri;
	private final String absoluteUri;
	private PropertyResourceBundle properties;
	
	/**
	 * 
	 * @param baseUri the base URI of the server
	 * @param basePath the base path of the server
	 * @param path the path to the properties file
	 * @throws IOException
	 */
	PropertiesResource(
		final URI baseUri,
		final Path basePath,
		final String baseName
	) throws IOException {
		super(baseName, basePath.resolve(baseName + DOT_PROPERTIES));
		
		String relative = basePath.relativize(path).toString();
		relative = relative.substring(0, relative.lastIndexOf(DOT_PROPERTIES));
		
		absoluteUri = baseUri.toString() + sha1 + "/" + relative + DOT_PROPERTIES;
		uri = baseUri.getPath() + sha1 + "/" + relative + DOT_PROPERTIES;
		
		properties =
			new PropertyResourceBundle(new StringReader(UTF_8.decode(ByteBuffer.wrap(bytes)).toString()));
	}
	
	public PropertyResourceBundle properties() {
		return properties;
	}
	
	@Override
	public String uri() {
		return uri;
	}
	
	public String absoluteUri() {
		return absoluteUri;
	}

	@Override
	public String mime() {
		return "text/plain; charset=UTF-8";
	}

}
