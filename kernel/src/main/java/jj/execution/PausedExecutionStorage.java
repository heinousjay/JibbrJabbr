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

import jj.util.Closer;

/**
 * A resumable handle to a paused execution, resume in try-with-resources like
 * <pre class="brush:java">
 * try (Closer closer = pes.resume()) { ... }
 * </pre>
 * @author jason
 */
public interface PausedExecutionStorage {
	
	Closer resume();
}