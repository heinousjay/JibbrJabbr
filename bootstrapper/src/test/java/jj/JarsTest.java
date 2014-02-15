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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class JarsTest {
	
	Path baseDir;
	Map<String, String> jarFsPropertiesMap;
	
	Map<String, List<String>> filesByJar;
	
	Map<String, URI> urisByJar;
	
	@Before
	public void before() throws IOException {
		jarFsPropertiesMap = new HashMap<>();
		jarFsPropertiesMap.put("create", "true");
		
		filesByJar = new HashMap<>();
		filesByJar.put("test1.jar", Arrays.asList(
			"/com/something/Init.class",
			"/com/something/whatever/Something.class",
			"/com/something/whatever/Otherthing.class",
			"/com/something/whatever/Otherthing.notaclass",
			"/com/something/whatever/deeper/Thing.class",
			"/META-INF/maven/pom.xml",
			"/META-INF/MANIFEST.MF"
		));
		filesByJar.put("test2.jar", Arrays.asList(
			"/com/otherthing/Init.class",
			"/com/otherthing/whatever/Something.class",
			"/com/otherthing/whatever/Otherthing.class",
			"/com/otherthing/whatever/deeper/Thing.class",
			"/com/otherthing/whatever/deeper/and/deeper/Thing.txt",
			"/META-INF/maven/pom.xml",
			"/META-INF/MANIFEST.MF"
		));
		
		baseDir = Files.createTempDirectory(JarsTest.class.getSimpleName().toLowerCase());
		
		urisByJar = new HashMap<>();
		for (String jarName : filesByJar.keySet()) {
			URI uri = URI.create("file:" + baseDir.resolve(jarName).toString());
			urisByJar.put(jarName, uri);
			uri = URI.create("jar:" + uri.toString());
			try (FileSystem fs = FileSystems.newFileSystem(uri, jarFsPropertiesMap)) {
				
				for (String path : filesByJar.get(jarName)) {
					Path file = fs.getPath(path);
					Files.createDirectories(file.getParent());
					Files.createFile(file);
				}
			}
		}
	}
	
	@After
	public void after() throws IOException {
		if (baseDir != null) {
			try {
			
				Files.walkFileTree(baseDir, new FileVisitor<Path>() {
		
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}
		
					@Override
					public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
						// try to delete the file anyway, even if its attributes
						// could not be read, since delete-only access is
						// theoretically possible
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}
		
					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						if (exc == null) {
							Files.delete(dir);
							return FileVisitResult.CONTINUE;
						}
						throw exc;
					}
		
					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						return FileVisitResult.CONTINUE;
					}
				});
				
				baseDir = null;
			} catch (IOException ioe) {
				System.err.println("unable to delete our temp directory! take care of this!");
				System.err.println(baseDir);
				throw ioe;
			}
		}
	}

	@Test
	public void test() throws IOException {
		Jars jars = new Jars(baseDir);
		
		for (String jarName : filesByJar.keySet()) {
			for (String file : filesByJar.get(jarName)) {
				Path path = jars.pathForFile(file);
				if (file.startsWith("/META-INF")) {
					assertThat(path, is(nullValue()));
				} else {
					assertThat(path, is(notNullValue()));
					assertThat(path.toUri().toString(), containsString(jarName));
					assertThat(jars.jarManifestForFile(file), is(notNullValue()));
					assertThat(jars.codeSourceForFile(file), is(notNullValue()));
					assertThat(jars.codeSourceForFile(file).getLocation(), is(urisByJar.get(jarName).toURL()));
				}
				
			}
		}
	}

}
