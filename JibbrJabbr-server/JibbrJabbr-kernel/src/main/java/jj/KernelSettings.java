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

import static jj.KernelSettings.Mode.DEV;

import org.slf4j.Logger;

/**
 * Provides settings for the kernel.  All options for all kernel managed objects
 * should be here, even if just hardcoded for now. There are big plans for this,
 * baby.
 * 
 * @author jason
 *
 */
public class KernelSettings {

	public static enum Mode {
		DEV,
		TEST,
		PROD
	}
	
	// probably moving this one out later, parsing services are
	// sure to be a bigun
	private static final class Parser {
		
		private final String input;
		
		Parser(String input) {
			this.input = input;
		}
		
		public int parseInt(int asDefault) {
			int result = asDefault;
			try {
				result = Integer.parseInt(input);
			} catch (Exception e) {}
			return result;
		}
	}
	
	public KernelSettings(Logger logger, String[] args) {
		logger.debug("Instantiating {}", KernelSettings.class);
		// for now, just looking for a port
		// later we can do something interesting...
		
		for (String arg : args) {
			logger.debug("Read argument {}", arg);
			Parser parser = new Parser(arg);
			if (parser.parseInt(0) != 0) {
				logger.debug("It's a port!");
				port = parser.parseInt(0);
			}
		}
	}
	
	private int port = 8080;
	public int port() {
		return port;
	}
	
	private int synchronousThreadCoreCount = 4;
	public int synchronousThreadCoreCount() {
		return synchronousThreadCoreCount;
	}
	
	private int synchronousThreadMaxCount = 20;
	public int synchronousThreadMaxCount() {
		return synchronousThreadMaxCount;
	}
	
	private long synchronousThreadTimeOut = 20L;
	public long synchronousThreadTimeOut() {
		return synchronousThreadTimeOut;
	}
	
	private int asynchronousThreadCoreCount = 4;
	public int asynchronousThreadCoreCount() {
		return asynchronousThreadCoreCount;
	}
	
	private int asynchronousThreadMaxCount = 20;
	public int asynchronousThreadMaxCount() {
		return asynchronousThreadMaxCount;
	}
	
	private long asynchronousThreadTimeOut = 20L;
	public long asynchronousThreadTimeOut() {
		return asynchronousThreadTimeOut;
	}
	
	private Mode mode = DEV;
	public Mode mode() {
		return mode;
	}
	
	private int httpMaxInitialLineLength = 2048;
	public int httpMaxInitialLineLength() {
		return httpMaxInitialLineLength;
	}
	
	private int httpMaxHeaderSize = 8192;
	public int httpMaxHeaderSize() {
		return httpMaxHeaderSize;
	}
	
	private int httpMaxChunkSize = 8192;
	public int httpMaxChunkSize() {
		return httpMaxChunkSize;
	}
	
	private int httpMaxRequestContentLength = 65536;
	public int httpMaxRequestContentLength() {
		return httpMaxRequestContentLength;
	}
	
	private int httpCompressionLevel = 6;
	public int httpCompressionLevel() {
		return httpCompressionLevel;
	}
	
	private int httpMaxShutdownTimeout = 30;
	public int httpMaxShutdownTimeout() {
		return httpMaxShutdownTimeout;
	}
}
