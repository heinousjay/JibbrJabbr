package jj.resource;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.file.Path;

import jj.execution.IOThread;

public class ScriptResource extends AbstractFileResource implements LoadedResource {
	
	private final ScriptResourceType type;
	private final String uri;
	private final String script;
	
	@IOThread
	ScriptResource(
		final ResourceCacheKey cacheKey,
		final ScriptResourceType type,
		final Path path,
		final String baseName
	) throws IOException {
		super(cacheKey, baseName, path);
		this.type = type;
		// our URI has our sha1 in it to allow for far-future caching
		uri = sha1 + "/" + baseName + type.suffix();
		script = byteBuffer.toString(UTF_8);
	}
	
	public ScriptResourceType type() {
		return type;
	}
	
	@Override
	public String uri() {
		return uri;
	}
	
	public String script() {
		return script;
	}

	@Override
	public String mime() {
		return "application/javascript; charset=UTF-8";
	}
	
	@Override
	public ByteBuf bytes() {
		return byteBuffer;
	}
	
	@Override
	Object[] creationArgs() {
		return new Object[] { type };
	};
}
