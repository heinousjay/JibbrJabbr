package jj.script.module;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Provider;

import org.mozilla.javascript.Script;

import jj.http.server.LoadedResource;
import jj.http.server.ServableResourceConfiguration;
import jj.resource.AbstractFileResource;
import jj.script.RhinoContext;

@ServableResourceConfiguration(routeContributor = ScriptResourceRouteContributor.class)
public class ScriptResource extends AbstractFileResource<Void> implements LoadedResource {
	
	private final String source;
	
	private final Script script;
	
	@Inject
	ScriptResource(
		final Dependencies dependencies,
		final Path path,
		final Provider<RhinoContext> contextProvider
	) throws IOException {
		super(dependencies, path);
		source = byteBuffer.toString(settings.charset());
		try (RhinoContext context = contextProvider.get()) {
			script = context.compileString(source, name());
		}
	}
	
	public Script script() {
		return script;
	}

	public String source() {
		return source;
	}
	
	@Override
	public ByteBuf bytes() {
		return Unpooled.wrappedBuffer(byteBuffer);
	}

	@Override
	public String contentType() {
		return settings.contentType();
	}

	@Override
	public boolean compressible() {
		return settings.compressible();
	}
}
