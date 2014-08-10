package jj.script.module;

import static java.nio.charset.StandardCharsets.UTF_8;
import static jj.configuration.resolution.AppLocation.Base;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Provider;
import org.mozilla.javascript.Script;

import jj.resource.AbstractFileResource;
import jj.resource.LoadedResource;
import jj.resource.MimeTypes;
import jj.resource.PathResolver;
import jj.script.RhinoContext;

public class ScriptResource extends AbstractFileResource implements LoadedResource {
	
	private final String source;
	
	private final Script script;
	
	private final boolean safeToServe;
	
	@Inject
	ScriptResource(
		final Dependencies dependencies,
		final Path path,
		final Provider<RhinoContext> contextProvider,
		final PathResolver pathResolver
	) throws IOException {
		super(dependencies, path);
		source = byteBuffer.toString(UTF_8);
		try (RhinoContext context = contextProvider.get()) {
			script = context.compileString(source, name);
		}
		
		// Public! soon
		safeToServe = base == Base && pathResolver.pathInBase(path);
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
	
	@Override
	public Charset charset() {
		return UTF_8;
	}
	
	@Override
	public String serverPath() {
		return "/" + sha1() + "/" + name();
	}
	
	@Override
	public boolean safeToServe() {
		return safeToServe;
	}
}
