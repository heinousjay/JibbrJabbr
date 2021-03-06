package jj.i18n;


import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import jj.resource.AbstractFileResource;

/**
 * 
 * @author jason
 *
 */
public class PropertiesResource extends AbstractFileResource<Void> {
	
	private final Map<String, String> properties;
	
	@Inject
	PropertiesResource(
		final Dependencies dependencies,
		final Path path
	) throws IOException {
		super(dependencies, path, false);
		
		Properties loader = new Properties();
		loader.load(new StringReader(new String(Files.readAllBytes(path), UTF_8)));
		Map<String, String> map = new HashMap<>();

		// little ugly! but properties are even uglier so we just use them
		// to parse
		for (Object key : loader.keySet()) {
			map.put(String.valueOf(key), String.valueOf(loader.get(key)));
		}
		
		properties = Collections.unmodifiableMap(map);
	}
	
	public Map<String, String> properties() {
		return properties;
	}
}
