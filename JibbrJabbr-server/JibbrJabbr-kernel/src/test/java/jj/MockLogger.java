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

import static jj.MockLogger.LogType.*;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * Simple logger that accumulates all logging activity in an
 * internal list for test verification, or ignorance, or whatever
 * 
 * @author Jason Miller
 *
 */
public class MockLogger implements Logger {

	public static enum LogType {
		Trace, Debug, Info, Warn, Error
	}
	
	public static class LogBundle {
		private final LogType logType;
		private final Marker marker;
		private final String message;
		private final Throwable throwable;
		private final Object[] args;
		
		private LogBundle(LogType logType, String message) {
			this(logType, null, message, null, (Object[])null);
		}
		
		private LogBundle(LogType logType, String message, Throwable throwable) {
			this(logType, null, message, throwable, (Object[])null);
		}
		
		private LogBundle(LogType logType, String message, Object arg) {
			this(logType, null, message, null, arg);
		}
		
		private LogBundle(LogType logType, String message, Object arg, Object arg2) {
			this(logType, null, message, null, arg, arg2);
		}
		
		private LogBundle(LogType logType, String message, Object[] args) {
			this(logType, null, message, null, args);
		}
		
		private LogBundle(LogType logType, Marker marker, String message, Throwable throwable, Object...args) {
			this.logType = logType;
			this.marker = marker;
			this.message = message;
			this.throwable = throwable;
			this.args = args;
			
			//System.out.println(this);
			if (throwable != null) {
				throwable.printStackTrace();
			}
		}
		
		public LogType logType() {
			return logType;
		}
		
		public Marker marker() {
			return marker;
		}
		
		public String message() {
			return message;
		}
		
		public Throwable throwable() {
			return throwable;
		}
		
		public Object[] args() {
			return args;
		}
		
		@Override
		public String toString() {
			return new StringBuilder(message.length() + 10)
				.append(logType)
				.append(' ')
				.append(message)
				.toString();
		}
	}
	
	private final ArrayList<LogBundle> messages = new ArrayList<>();
	
	public LogBundle[] messages() {
		LogBundle[] result = messages.toArray(new LogBundle[messages.size()]);
		messages.clear();
		return result;
	}
	
	@Override
	public String getName() {
		return MockLogger.class.getSimpleName();
	}

	@Override
	public boolean isTraceEnabled() {
		return true;
	}

	@Override
	public void trace(String msg) {
		messages.add(new LogBundle(Trace, msg));
	}

	@Override
	public void trace(String format, Object arg) {
		messages.add(new LogBundle(Trace, format, arg));
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		messages.add(new LogBundle(Trace, format, arg1, arg2));
	}
	
	@Override
	public void trace(String format, Object[] argArray) {
		messages.add(new LogBundle(Trace, format, argArray));
	}

	@Override
	public void trace(String msg, Throwable t) {
		messages.add(new LogBundle(Trace, msg, t));
	}

	@Override
	public boolean isTraceEnabled(Marker marker) {
		return true;
	}
	
	@Override
	public void trace(Marker marker, String msg) {
		messages.add(new LogBundle(Trace, marker, msg, null));
	}

	@Override
	public void trace(Marker marker, String format, Object arg) {
		messages.add(new LogBundle(Trace, marker, format, null, arg));
	}

	@Override
	public void trace(Marker marker, String format, Object arg1, Object arg2) {
		messages.add(new LogBundle(Trace, marker, format, null, arg1, arg2));
	}

	@Override
	public void trace(Marker marker, String format, Object[] argArray) {
		messages.add(new LogBundle(Trace, marker, format, null, argArray));
	}

	@Override
	public void trace(Marker marker, String msg, Throwable t) {
		messages.add(new LogBundle(Trace, marker, msg, t));
	}

	@Override
	public boolean isDebugEnabled() {
		return true;
	}

	@Override
	public void debug(String msg) {
		messages.add(new LogBundle(Debug, msg));
	}

	@Override
	public void debug(String format, Object arg) {
		messages.add(new LogBundle(Debug, format, arg));
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		messages.add(new LogBundle(Debug, format, arg1, arg2));
	}
	
	@Override
	public void debug(String format, Object[] argArray) {
		messages.add(new LogBundle(Debug, format, argArray));
	}

	@Override
	public void debug(String msg, Throwable t) {
		messages.add(new LogBundle(Debug, msg, t));
	}

	@Override
	public boolean isDebugEnabled(Marker marker) {
		return true;
	}

	@Override
	public void debug(Marker marker, String msg) {
		messages.add(new LogBundle(Debug, marker, msg, null));
	}

	@Override
	public void debug(Marker marker, String format, Object arg) {
		messages.add(new LogBundle(Debug, marker, format, null, arg));
	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		messages.add(new LogBundle(Debug, marker, format, null, arg1, arg2));
	}

	@Override
	public void debug(Marker marker, String format, Object[] argArray) {
		messages.add(new LogBundle(Debug, marker, format, null, argArray));
	}

	@Override
	public void debug(Marker marker, String msg, Throwable t) {
		messages.add(new LogBundle(Debug, marker, msg, t));
	}

	@Override
	public boolean isInfoEnabled() {
		return true;
	}
	
	@Override
	public void info(String msg) {
		messages.add(new LogBundle(Info, msg));
	}
	
	@Override
	public void info(String format, Object arg) {
		messages.add(new LogBundle(Info, format, arg));
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		messages.add(new LogBundle(Info, format, arg1, arg2));
	}

	@Override
	public void info(String format, Object[] argArray) {
		messages.add(new LogBundle(Info, format, argArray));
	}

	@Override
	public void info(String msg, Throwable t) {
		messages.add(new LogBundle(Info, msg, t));
	}
	
	@Override
	public boolean isInfoEnabled(Marker marker) {
		return true;
	}

	@Override
	public void info(Marker marker, String msg) {
		messages.add(new LogBundle(Info, marker, msg, null));
	}
	
	@Override
	public void info(Marker marker, String format, Object arg) {
		messages.add(new LogBundle(Info, marker, format, null, arg));
	}

	@Override
	public void info(Marker marker, String format, Object arg1, Object arg2) {
		messages.add(new LogBundle(Info, marker, format, null, arg1, arg2));
	}

	@Override
	public void info(Marker marker, String format, Object[] argArray) {
		messages.add(new LogBundle(Info, marker, format, null, argArray));
	}

	@Override
	public void info(Marker marker, String msg, Throwable t) {
		messages.add(new LogBundle(Info, marker, msg, t));
	}

	@Override
	public boolean isWarnEnabled() {
		return true;
	}

	@Override
	public void warn(String msg) {
		messages.add(new LogBundle(Warn, msg));
	}

	@Override
	public void warn(String format, Object arg) {
		messages.add(new LogBundle(Warn, format, arg));
	}

	@Override
	public void warn(String format, Object[] argArray) {
		messages.add(new LogBundle(Warn, format, argArray));
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		messages.add(new LogBundle(Warn, format, arg1, arg2));
	}

	@Override
	public void warn(String msg, Throwable t) {
		messages.add(new LogBundle(Warn, msg, t));
	}

	@Override
	public boolean isWarnEnabled(Marker marker) {
		return true;
	}

	@Override
	public void warn(Marker marker, String msg) {
		messages.add(new LogBundle(Warn, marker, msg, null));
	}

	@Override
	public void warn(Marker marker, String format, Object arg) {
		messages.add(new LogBundle(Warn, marker, format, null, arg));
	}

	@Override
	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		messages.add(new LogBundle(Warn, marker, format, null, arg1, arg2));
	}

	@Override
	public void warn(Marker marker, String format, Object[] argArray) {
		messages.add(new LogBundle(Warn, marker, format, null, argArray));
	}

	@Override
	public void warn(Marker marker, String msg, Throwable t) {
		messages.add(new LogBundle(Warn, marker, msg, t));
	}

	@Override
	public boolean isErrorEnabled() {
		return true;
	}

	@Override
	public void error(String msg) {
		messages.add(new LogBundle(Error, msg));
	}

	@Override
	public void error(String format, Object arg) {
		messages.add(new LogBundle(Error, format, arg));
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		messages.add(new LogBundle(Error, format, arg1, arg2));
	}

	@Override
	public void error(String format, Object[] argArray) {
		messages.add(new LogBundle(Error, format, argArray));
	}

	@Override
	public void error(String msg, Throwable t) {
		messages.add(new LogBundle(Error, msg, t));
	}

	@Override
	public boolean isErrorEnabled(Marker marker) {
		return true;
	}

	@Override
	public void error(Marker marker, String msg) {
		messages.add(new LogBundle(Error, marker, msg, null));
	}

	@Override
	public void error(Marker marker, String format, Object arg) {
		messages.add(new LogBundle(Error, marker, format, null, arg));
	}

	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) {
		messages.add(new LogBundle(Error, marker, format, null, arg1, arg2));
	}

	@Override
	public void error(Marker marker, String format, Object[] argArray) {
		messages.add(new LogBundle(Error, marker, format, null, argArray));
	}

	@Override
	public void error(Marker marker, String msg, Throwable t) {
		messages.add(new LogBundle(Error, marker, msg, t));
	}

}
