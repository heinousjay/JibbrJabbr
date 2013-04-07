package jj.resource;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import jj.IOThread;

public class ScriptResource extends AbstractFileResource {
	
	private final ScriptResourceType type;
	private final String uri;
	private final String absoluteUri;
	private final String script;
	
	@IOThread
	ScriptResource(
		final ScriptResourceType type,
		final Path path,
		final String baseName,
		final URI baseUri
	) throws IOException {
		super(baseName, path);
		this.type = type;
		// our URI has our sha1 in it to allow for far-future caching
		uri = baseName + "/" + sha1 + type.suffix();
		absoluteUri = baseUri.toString() + uri;
		script = UTF_8.decode(ByteBuffer.wrap(bytes)).toString();
	}
	
	public ScriptResourceType type() {
		return type;
	}
	
	@Override
	public Object[] creationArgs() {
		return new Object[] { type };
	};
	
	@Override
	public String uri() {
		return uri;
	}
	
	public String absoluteUri() {
		return absoluteUri;
	}
	
	public String script() {
		return script;
	}

	@Override
	public String mime() {
		return "application/javascript; charset=UTF-8";
	}
}
