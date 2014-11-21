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
package jj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jj.execution.JJTask;
import jj.logging.LoggedEvent;

import org.slf4j.Logger;

/**
 * @author jason
 *
 */
@ServerLogger
public class ServerStarting extends LoggedEvent {
	
	public enum Priority {
		Highest,
		NearHighest,
		Middle,
		NearLowest,
		Lowest;
	}
	
	private final Version version;
	
	private final HashMap<Priority, List<JJTask>> startupTasks = new HashMap<>();
	
	ServerStarting(Version version) {
		this.version = version;
	}

	public void registerStartupTask(final Priority priority, final JJTask task) {
		startupTasks.computeIfAbsent(priority, (p) -> { return new ArrayList<>(1); }).add(task);
	}

	Map<Priority, List<JJTask>> startupTasks() {
		return startupTasks;
	}

	@Override
	public void describeTo(Logger logger) {
		logger.info("Welcome to {} version {} commit {}", version.name(), version.version(), version.commitId());
		logger.info("Starting the server");
	}

}
