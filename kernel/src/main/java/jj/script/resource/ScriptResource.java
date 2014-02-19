package jj.script.resource;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.AbstractFileResource;
import jj.resource.LoadedResource;
import jj.resource.ResourceCacheKey;

@Singleton
public class ScriptResource extends AbstractFileResource implements LoadedResource {
	
	private final String script;
	
	@Inject
	ScriptResource(
		final ResourceCacheKey cacheKey,
		final Path path,
		final String baseName
	) throws IOException {
		super(cacheKey, baseName, path);
		script = byteBuffer.toString(UTF_8);
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
		return Unpooled.wrappedBuffer(byteBuffer);
	}
}
