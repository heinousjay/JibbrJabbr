/*
 *    Copyright 2012 Jason Miller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jj.css;

import static java.nio.charset.StandardCharsets.UTF_8;
import static jj.configuration.resolution.AppLocation.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Provider;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import jj.resource.AbstractResource;
import jj.resource.LoadedResource;
import jj.resource.MimeTypes;
import jj.resource.NoSuchResourceException;
import jj.resource.ResourceNotViableException;
import jj.resource.stat.ic.StaticResource;
import jj.script.Global;
import jj.script.RhinoContext;
import jj.script.module.ScriptResource;
import jj.util.SHA1Helper;

/**
 * <p>
 * a virtual resource representing either a static css
 * stylesheet, or a stylesheet that has run through less
 * processing.
 * 
 * @author jason
 *
 */
public class StylesheetResource extends AbstractResource implements LoadedResource {

	static final String LESS_SCRIPT = "less-rhino-1.7.3.js";
	private final String name;
	private final ByteBuf bytes;
	private final String sha1;
	private final Path path;
	private final long size;
	private final LessConfiguration lessConfiguration;

	@Inject
	StylesheetResource(
		final Dependencies dependencies,
		final String name,
		final Provider<RhinoContext> contextProvider,
		final @Global ScriptableObject global,
		final CssReferenceVersionProcessor processor,
		final LessConfiguration lessConfiguration
	) {
		super(dependencies);
		
		this.name = name;
		this.lessConfiguration = lessConfiguration;
		
		// is there a static css file?
		StaticResource css = resourceFinder.loadResource(StaticResource.class, Base, name);
		String result;
		
		if (css == null) {
			// for whatever reason, i'm getting obsessed with optimal string processing
			String lessName = new StringBuilder(name.length() + 1)
				.append(name)
				.replace(name.length() - 3, name.length() - 2, "le")
				.toString();

			// this is just to check for existence of the resource, it will get loaded from
			// the script execution and hooked into the dependency system then
			LessResource lessSheet = resourceFinder.loadResource(LessResource.class, Base, lessName);
			if (lessSheet == null) {
				throw new NoSuchResourceException(StylesheetResource.class, name());
			}
			path = lessSheet.path();
			result = processLessScript(contextProvider, global, lessName);
			
			if (result == null) {
				throw new ResourceNotViableException(path, "could not process " + lessName);
			}
			
		} else {
			path = css.path();
			css.addDependent(this);
			// somehow, hook up a watch for the less file too. 
			try {
				result = new String(Files.readAllBytes(path), UTF_8);
			} catch (IOException ioe) {
				throw new ResourceNotViableException(path, ioe);
			}
		}

		result = processor.fixUris(result, this);

		sha1 = SHA1Helper.keyFor(result);
		bytes = Unpooled.copiedBuffer(result, UTF_8);
		size = bytes.readableBytes();
	}
	
	private String processLessScript(final Provider<RhinoContext> contextProvider, final ScriptableObject global, String lessName) {
		
		publisher.publish(new StartingLessProcessing(lessName));
		
		try (RhinoContext context = contextProvider.get().withoutContinuations()) {
			// turn on optimizations before loading this!
			ScriptResource lessScript = resourceFinder.loadResource(ScriptResource.class, Assets, LESS_SCRIPT);
			assert lessScript != null : "less script not found! build failure!";
			
			lessScript.addDependent(this);
			
			ScriptableObject local = context.newObject(global);
			local.setPrototype(global);
			local.setParentScope(null);
			
			local.defineProperty("readFile", new ReadFileFunction(), ScriptableObject.EMPTY);
			local.defineProperty("name", new NameFunction(), ScriptableObject.EMPTY);
			local.defineProperty("lessLog", new LessLogFunction(), ScriptableObject.EMPTY);
			
			context.executeScript(lessScript.script(), local);

			Function runLess = (Function)local.get("runLess", local);
			Object result = context.callFunction(runLess, local, local, lessName, lessConfiguration);
			
			if (result == Scriptable.NOT_FOUND) {
				return null;
			}
			
			return String.valueOf(result);
		} finally {
			NameFunction.name.set(null);
			publisher.publish(new FinishedLessProcessing(lessName));
		}
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String uri() {
		return "/" + sha1() + "/" + name;
	}

	@Override
	public String sha1() {
		return sha1;
	}

	@Override
	public long size() {
		return size;
	}
	
	Path path() {
		return path;
	}

	@Override
	public String mime() {
		return MimeTypes.get("css");
	}

	@Override
	public ByteBuf bytes() {
		return Unpooled.wrappedBuffer(bytes);
	}

	@Override
	public boolean needsReplacing() throws IOException {
		// always replaced by whatever our resources are
		return false;
	}
	
	@Override
	protected boolean removeOnReload() {
		return false;
	}
	
	// the tiny API to connect the less script to the resource system
	
	private class ReadFileFunction extends BaseFunction {
		
		private static final long serialVersionUID = -1L;
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			String resourceName = String.valueOf(args[0]);
			publisher.publish(new LoadingLessResource(resourceName));
			LessResource lr = resourceFinder.loadResource(LessResource.class, Base, resourceName);
			if (lr != null) {
				lr.addDependent(StylesheetResource.this);
				return lr.contents();
			} 
			
			publisher.publish(new LessResourceNotFound(resourceName));
			return "";
		}
	}
	
	private static class NameFunction extends BaseFunction {
		
		private static final long serialVersionUID = -1L;
		
		private static ThreadLocal<String> name = new ThreadLocal<>();
		
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length == 1) {
				name.set(String.valueOf(args[0]));
			}
			return name.get() == null ? Undefined.instance : name.get();
		}
	}
	
	private class LessLogFunction extends BaseFunction {
		
		private static final long serialVersionUID = -1L;
		
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			
			System.out.println(args[0]);
			return Undefined.instance;
		}
	}
}