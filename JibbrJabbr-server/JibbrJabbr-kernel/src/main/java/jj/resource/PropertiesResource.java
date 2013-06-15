package jj.resource;


import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.PropertyResourceBundle;

public class PropertiesResource extends AbstractFileResource {

	private static final String DOT_PROPERTIES = ".properties";
	
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
		final Path basePath,
		final String baseName
	) throws IOException {
		super(baseName, basePath.resolve(baseName + DOT_PROPERTIES));
		
		String relative = basePath.relativize(path).toString();
		relative = relative.substring(0, relative.lastIndexOf(DOT_PROPERTIES));
		
		uri = sha1 + "/" + baseName;
		
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

	@Override
	public String mime() {
		return "text/plain; charset=UTF-8";
	}

}
