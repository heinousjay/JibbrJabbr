package jj.resource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import jj.Configuration;

class ScriptResourceCreator implements ResourceCreator<ScriptResource> {

	private final URI baseUri;
	private final Path basePath;
	
	ScriptResourceCreator(final Configuration configuration) {
		this.baseUri = configuration.baseUri();
		this.basePath = configuration.basePath();
	}
	
	@Override
	public Class<ScriptResource> type() {
		return ScriptResource.class;
	}

	@Override
	public Path toPath(String baseName, Object... args) {
		if (args.length != 1 || !(args[0] instanceof ScriptResourceType)) {
			throw new IllegalArgumentException();
		}
		ScriptResourceType type = (ScriptResourceType)args[0];
		return basePath.resolve(baseName + type.suffix());
	}

	@Override
	public ScriptResource create(String baseName, Object... args) throws IOException {
		if (args.length != 1 || !(args[0] instanceof ScriptResourceType)) {
			throw new IllegalArgumentException();
		}
		Path path = toPath(baseName, args);
		ScriptResourceType type = (ScriptResourceType)args[0];
		return new ScriptResource(type, path, baseName, baseUri);
	}

}
