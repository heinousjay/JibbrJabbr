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
package jj.repl;

import static jj.server.ServerLocation.*;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.util.HashMap;

import javax.inject.Inject;

import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import jj.script.AbstractScriptEnvironment;
import jj.script.PendingKey;
import jj.script.Global;
import jj.script.module.RootScriptEnvironment;
import jj.script.module.ScriptResource;
import jj.util.Closer;

/**
 * <p>
 * Execution environment for REPL connections. Main purpose is as a coordination
 * point for continuations and a parent for modules
 * 
 * @author jason
 *
 */
class ReplScriptEnvironment extends AbstractScriptEnvironment implements RootScriptEnvironment {

	static final String NAME = "repl";
	static final String BASE_REPL_SYSTEM = "base-repl-system.js";
	
	private final CurrentReplChannelHandlerContext currentCtx;
	private final ScriptableObject global;
	private final ScriptableObject local;
	private final ScriptResource system;
	
	private final HashMap<PendingKey, ChannelHandlerContext> pendingContexts = new HashMap<>();
	
	@Inject
	ReplScriptEnvironment(
		final Dependencies dependencies,
		final @Global ScriptableObject global,
		final CurrentReplChannelHandlerContext currentCtx
	) {
		super(dependencies);
		
		this.currentCtx = currentCtx;
		
		this.global = global;
		this.local = 
			configureInjectFunction(
				configureTimers(
					configureModuleObjects(NAME, createChainedScope(global))
				)
			);
		
		system = resourceFinder.loadResource(ScriptResource.class, Assets, BASE_REPL_SYSTEM);
		
		assert system != null : "can't find " + BASE_REPL_SYSTEM + ", build is broken!";
		
		system.addDependent(this);
	}

	@Override
	protected void captureContextForKey(PendingKey key) {
		pendingContexts.put(key, currentCtx.current());
	}
	
	@Override
	protected Closer restoreContextForKey(PendingKey key) {
		ChannelHandlerContext ctx = pendingContexts.remove(key);
		assert ctx != null : "no ctx found to restore";
		
		return currentCtx.enterScope(ctx);
	}
	
	@Override
	public Scriptable scope() {
		return local;
	}

	@Override
	public Script script() {
		return system.script();
	}

	@Override
	public String scriptName() {
		return NAME;
	}

	@Override
	public String sha1() {
		return system.sha1();
	}

	@Override
	public boolean needsReplacing() throws IOException {
		return false;
	}

	@Override
	protected boolean removeOnReload() {
		return false;
	}

	@Override
	public ScriptableObject global() {
		return global;
	}
}
