package jj.resource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

class ScriptResourceCreator implements ResourceCreator<ScriptResource> {

	private final URI baseUri;
	private final Path basePath;
	
	ScriptResourceCreator(final URI baseUri, final Path basePath) {
		this.baseUri = baseUri;
		this.basePath = basePath;
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
