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
package logging;

/**
 * Inspired by the slf4j LocLogger but without
 * the string backups.  don't need em
 * 
 * @author jason
 *
 */
public interface Logger {

	void trace(Enum<?> key, Object... args);
	void debug(Enum<?> key, Object... args);
	void info(Enum<?> key, Object... args);
	void warn(Enum<?> key, Throwable t);
	void warn(Enum<?> key, Object... args);
	void error(Enum<?> key, Throwable t);
	void error(Enum<?> key, Object... args);
}
