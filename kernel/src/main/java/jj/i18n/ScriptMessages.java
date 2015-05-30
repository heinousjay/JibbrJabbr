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
package jj.i18n;

import static jj.server.ServerLocation.Virtual;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import jj.execution.TaskRunner;
import jj.resource.ResourceFinder;
import jj.resource.ResourceTask;
import jj.script.Continuation;
import jj.script.PendingKey;
import jj.script.ContinuationProcessor;
import jj.script.ContinuationState;
import jj.script.CurrentScriptEnvironment;
import jj.util.StringUtils;

/**
 * Looks up a {@link MessagesResource} for a calling script, possibly
 * causing a continuation if it needs one
 * @author jason
 *
 */
@Singleton
public class ScriptMessages implements ContinuationProcessor {
	
	static class ScriptMessagesLoaderBundle implements Continuation {

		private PendingKey pendingKey;
		
		private final String name;
		private final Locale locale;
		private final Scriptable scope;
		
		ScriptMessagesLoaderBundle(final String name, final Locale locale, final Scriptable scope) {
			this.name = name;
			this.locale = locale;
			this.scope = scope;
		}
		
		@Override
		public String toString() {
			return "name: " + name + ", locale: " + locale;
		}
		
		@Override
		public void pendingKey(PendingKey pendingKey) {
			this.pendingKey = pendingKey;
		}

		@Override
		public PendingKey pendingKey() {
			return pendingKey;
		}
		
	}

	private final ResourceFinder resourceFinder;
	private final TaskRunner taskRunner;
	private final CurrentScriptEnvironment env;
	private final I18NConfiguration configuration;
	
	@Inject
	ScriptMessages(
		final ResourceFinder resourceFinder,
		final TaskRunner taskRunner,
		final CurrentScriptEnvironment env,
		final I18NConfiguration configuration
	) {
		this.resourceFinder = resourceFinder;
		this.taskRunner = taskRunner;
		this.env = env;
		this.configuration = configuration;
	}
	
	private Locale figureLocale(final String languageTag) {
		
		Locale locale;
		
		if (StringUtils.isEmpty(languageTag)) {
			locale = configuration.defaultLocale();
		} else {
			locale = Locale.forLanguageTag(languageTag.replace('_', '-')); // being nice - we accept _ and - in the API
		}
		
		if (locale == null || "und".equals(locale.toLanguageTag())) {
			locale = Locale.getDefault();
		}
		
		return locale;
	}
	
	private String figureName(final String name) {
		if (StringUtils.isEmpty(name)) {
			return env.current().name();
		}
		
		return name;
	}
	
	public Scriptable getMessagesResource(final String inputName, final String languageTag) {
		
		// first, we had better be in a script
		assert env.current() != null : "don't use this outside a script!";
		
		// if no name, the current script environment name
		// if no language tag, the current user's locale.  if no current user, the default locale
		
		// how to distinguish names and locales?  argh.  if only one is given, see if it looks like a locale?
		// I DON'T KNOW - i guess it's whichever makes the most sense from an API perspective.  OR if i can make it somehow
		
		
		Locale locale = figureLocale(languageTag);
		
		String name = figureName(inputName);
		
		MessagesResource resource = resourceFinder.findResource(MessagesResource.class, Virtual, name, locale);
		
		Scriptable scope = env.current().scope();
		
		// if we can't find it, look it up
		if (resource == null) {
			throw env.preparedContinuation(new ScriptMessagesLoaderBundle(name, locale, scope));
		}
		
		return new MessagesScriptable(resource, scope);
		
	}

	@Override
	public void process(final ContinuationState continuationState) {
		
		final ScriptMessagesLoaderBundle bundle = continuationState.continuationAs(ScriptMessagesLoaderBundle.class);
		taskRunner.execute(new ResourceTask("loading messages " + bundle.name + "_" + bundle.locale) {
			
			@Override
			protected void run() throws Exception {
				MessagesResource resource = resourceFinder.loadResource(MessagesResource.class, Virtual, bundle.name, bundle.locale);
				
				bundle.pendingKey.resume(resource == null ? Undefined.instance : new MessagesScriptable(resource, bundle.scope));
			}
		});
	}
}
