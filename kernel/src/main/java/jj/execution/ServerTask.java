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
package jj.execution;

/**
 * A task for internal server activities.  Supports execution after a number
 * of milliseconds, denoted by overriding {@link ServerTask#delay()} with a positive
 * response. The default of 0 means to schedule immediately. a negative number means
 * to throw an AssertionError when you get scheduled and not run and print a nasty error
 * to the console, so don't
 * 
 * @author jason
 *
 */
public abstract class ServerTask extends DelayedTask<ServerExecutor> {

	public ServerTask(String name) {
		super(name);
	}
}
