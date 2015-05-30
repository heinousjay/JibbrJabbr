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
package jj.logging;

/**
 * @author jason
 *
 */
public enum Level {
	Off {
		@Override
		ch.qos.logback.classic.Level logbackLevel() {
			return ch.qos.logback.classic.Level.OFF;
		}
	},
	Error {
		@Override
		ch.qos.logback.classic.Level logbackLevel() {
			return ch.qos.logback.classic.Level.ERROR;
		}
	},
	Warn {
		@Override
		ch.qos.logback.classic.Level logbackLevel() {
			return ch.qos.logback.classic.Level.WARN;
		}
	},
	Info {
		@Override
		ch.qos.logback.classic.Level logbackLevel() {
			return ch.qos.logback.classic.Level.INFO;
		}
	},
	Debug {
		@Override
		ch.qos.logback.classic.Level logbackLevel() {
			return ch.qos.logback.classic.Level.DEBUG;
		}
	},
	Trace {
		@Override
		ch.qos.logback.classic.Level logbackLevel() {
			return ch.qos.logback.classic.Level.TRACE;
		}
	};
	
	abstract ch.qos.logback.classic.Level logbackLevel();
}
