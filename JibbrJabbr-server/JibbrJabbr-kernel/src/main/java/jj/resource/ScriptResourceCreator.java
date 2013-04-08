package jj.resource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.Configuration;

@Singleton
class ScriptResourceCreator implements ResourceCreator<ScriptResource> {

	private final URI baseUri;
	private final Path basePath;
	
	@Inject
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
			throw new IllegalArgumentException("expected ScriptResourceType but got " + (args.length > 0 ? args[0] : " null"));
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
