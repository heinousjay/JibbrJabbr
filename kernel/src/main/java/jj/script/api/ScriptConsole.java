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
package jj.script.api;

import static jj.logging.Level.*;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.event.Publisher;
import jj.logging.Level;
import jj.script.CurrentScriptEnvironment;

/**
 * @author jason
 *
 */
@Singleton
public class ScriptConsole {
	
	private final CurrentScriptEnvironment env;
	private final Publisher publisher;
	
	@Inject
	ScriptConsole(final CurrentScriptEnvironment env, final Publisher publisher) {
		this.env = env;
		this.publisher = publisher;
	}
	
	private void publish(Level level, List<String> args) {
		assert env.current() != null;
		publisher.publish(new ConsoleLoggingEvent(env.currentRootScriptEnvironment().name(), level, args));
	}
	
	public void trace(List<String> args) {
		publish(Trace, args);
	}
	
	public void debug(List<String> args) {
		publish(Debug, args);
	}
	
	public void info(List<String> args) {
		publish(Info, args);
	}
	
	public void warn(List<String> args) {
		publish(Warn, args);
	}
	
	public void error(List<String> args) {
		publish(Error, args);
	}
}
