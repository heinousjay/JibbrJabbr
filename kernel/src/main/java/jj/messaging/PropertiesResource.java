package jj.messaging;


import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.AppLocation;
import jj.resource.AbstractFileResource;
import jj.resource.ResourceCacheKey;

/**
 * 
 * @author jason
 *
 */
@Singleton
public class PropertiesResource extends AbstractFileResource {
	
	private final HashMap<String, String> properties;
	
	@Inject
	PropertiesResource(
		final ResourceCacheKey cacheKey,
		final Path path,
		final AppLocation base,
		final String name
	) throws IOException {
		super(cacheKey, base, name, path);
		
		Properties loader = new Properties();
		loader.load(new StringReader(byteBuffer.toString(UTF_8)));

		properties = new HashMap<>();

		// little ugly!
		for (Object key : loader.keySet()) {
			properties.put(String.valueOf(key), String.valueOf(loader.get(key)));
		}
	}
	
	public HashMap<String, String> properties() {
		return properties;
	}
	
	@Override
	public String mime() {
		return "text/plain; charset=UTF-8";
	}

}
