package jj.script.module;

import static java.nio.charset.StandardCharsets.UTF_8;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.mozilla.javascript.Script;

import jj.resource.AbstractFileResource;
import jj.resource.LoadedResource;
import jj.resource.MimeTypes;
import jj.script.RhinoContext;

@Singleton
public class ScriptResource extends AbstractFileResource implements LoadedResource {
	
	private final String source;
	
	private final Script script;
	
	@Inject
	ScriptResource(
		final Dependencies dependencies,
		final Path path,
		final Provider<RhinoContext> contextProvider
	) throws IOException {
		super(dependencies, path);
		source = byteBuffer.toString(UTF_8);
		try (RhinoContext context = contextProvider.get()) {
			script = context.compileString(source, name);
		}
	}
	
	public Script script() {
		return script;
	}

	public String source() {
		return source;
	}

	@Override
	public String mime() {
		return MimeTypes.get("js");
	}
	
	@Override
	public ByteBuf bytes() {
		return Unpooled.wrappedBuffer(byteBuffer);
	}
}
