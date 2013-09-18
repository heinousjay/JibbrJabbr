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
package jj.resource.script.environment;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.SHA1Helper;
import jj.execution.IOThread;
import jj.execution.JJExecutors;
import jj.execution.JJRunnable;
import jj.resource.AbstractResourceBase;
import jj.resource.MimeTypes;
import jj.resource.NoSuchResourceException;
import jj.resource.ResourceCacheKey;
import jj.resource.ResourceFinder;
import jj.resource.html.HtmlResource;
import jj.resource.script.ScriptResource;
import jj.resource.script.ScriptResourceType;

/**
 * @author jason
 *
 */
@Singleton
public class DocumentScriptEnvironment extends AbstractResourceBase {

	private final String baseName;
	
	final JJExecutors executors;
	
	private final HtmlResource html;
	
	private final ScriptResource clientScript;
	private final ScriptResource sharedScript;
	private final ScriptResource serverScript;
	
	private final String sha1;
	
	/**
	 * @param cacheKey
	 */
	@Inject
	DocumentScriptEnvironment(
		final ResourceCacheKey cacheKey,
		final String baseName,
		final ResourceFinder resourceFinder,
		final JJExecutors executors
	) {
		super(cacheKey);
		this.baseName = baseName;
		this.executors = executors;
		
		html = resourceFinder.loadResource(HtmlResource.class, baseName); // + html!
		
		if (html == null) throw new NoSuchResourceException(baseName);
		
		clientScript = resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Client.suffix(baseName));
		sharedScript = resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Shared.suffix(baseName));
		serverScript = resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Server.suffix(baseName));
		
		dependsOn(html);
		if (clientScript != null) dependsOn(clientScript);
		if (sharedScript != null) dependsOn(sharedScript);
		if (serverScript != null) dependsOn(serverScript);
		
		sha1 = SHA1Helper.keyFor(
			html.sha1(),
			clientScript == null ? "none" : clientScript.sha1(),
			sharedScript == null ? "none" : sharedScript.sha1(),
			serverScript == null ? "none" : serverScript.sha1()
		);
		
		executors.scriptExecutorFor(baseName).submit(new JJRunnable("") {
			@Override
			public void doRun() {
				initialize();
			}
		});
	}
	
	private void initialize() {
	}

	@Override
	public String baseName() {
		return baseName;
	}

	@Override
	public String uri() {
		return "/" + baseName;
	}

	@Override
	public String sha1() {
		return sha1;
	}

	@Override
	public String mime() {
		return MimeTypes.get(".html");
	}

	@Override
	@IOThread
	protected boolean needsReplacing() throws IOException {
		// this never goes out of scope on its own
		// dependency tracking handles it all 
		return false;
	}

}
