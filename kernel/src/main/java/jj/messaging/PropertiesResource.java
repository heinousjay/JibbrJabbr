package jj.messaging;


import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.AbstractFileResource;

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
		final Dependencies dependencies,
		final Path path,
		final String name
	) throws IOException {
		super(dependencies, name, path);
		
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
