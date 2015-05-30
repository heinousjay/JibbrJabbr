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
package jj.testing;

import static java.nio.charset.StandardCharsets.*;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

enum HttpTraceMode {
	Nothing,
	Verifying,
	Recording;
	
	private static final Map<String, String> env = Collections.singletonMap("create", "true");
	
	private static FileSystem jar(String invoker, boolean throwOnError) throws Exception {
		Path path = Paths.get("src/test/resources/test-data", invoker + ".jar");
		
		if (throwOnError && !Files.exists(path)) {
			throw new AssertionError("You need to record this test first.");
		} else if (!throwOnError && Files.exists(path)) {
			Files.delete(path);
		} else if (!throwOnError) {
			Files.createDirectories(path.getParent());
		}
		
		return FileSystems.newFileSystem(URI.create("jar:" + path.toUri().toString()), env);
	}
	
	private static volatile FileSystem jarFs;
	
	private static ConcurrentHashMap<String, byte[]> byteCache = new ConcurrentHashMap<>();
	
	public HttpTraceMode traceEvent(String name, byte[] bytes) throws Exception {
		Path path = jarFs.getPath(name);
		
		switch(this) {
		case Verifying:
			if (!Files.exists(path)) {
				throw new AssertionError("cannot find " + path.toUri());
			}
			if (!Arrays.equals(bytes, byteCache.computeIfAbsent(name, key -> {
				try {
					return Files.readAllBytes(path);
				} catch (Exception e) {
					throw new AssertionError(e);
				}
			}))) {
				System.out.println("OUTPUT:");
				System.out.println(new String(bytes, UTF_8));
				
				throw new AssertionError(name + " does not match the recorded output");
			}
			
			break;
			
		case Recording:
			if (!Files.exists(path)) {
				Files.createDirectories(path.getParent());
				Files.write(path, bytes);
			}
			break;
		
		case Nothing:
		default:
			
			throw new AssertionError("you should not be calling this without setting a mode first");
		}
		
		return this;
	}
	
	public JibbrJabbrTestStatement traceStatement(JibbrJabbrTestStatement inner, String invoker) {
		switch(this) {
		case Verifying:
			return new JibbrJabbrTestStatement(inner) {

				@Override
				public void evaluate() throws Throwable {
					try (FileSystem jar = jar(invoker, true)) {
						jarFs = jar;
						evaluateInner();
					}
				}
				
			};
			
		case Recording:
			return new JibbrJabbrTestStatement(inner) {

				@Override
				public void evaluate() throws Throwable {
					try (FileSystem jar = jar(invoker, false)) {
						jarFs = jar;
						evaluateInner();
					}
				}
				
			};
			
		case Nothing:
		default:
			return inner;
		}
	}
}