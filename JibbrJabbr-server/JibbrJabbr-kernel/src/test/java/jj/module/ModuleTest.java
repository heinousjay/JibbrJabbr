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
package jj.module;

import static org.junit.Assert.*;

import java.net.URI;

import jj.FileSystemServiceRule;
import jj.JJ;

import org.junit.Rule;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class ModuleTest {
	
	@Rule
	public FileSystemServiceRule fssRule = new FileSystemServiceRule();
	
	private final URI clamwhoresJar;
	
	
	public ModuleTest() throws Exception {
		
		// this looks a little ugly but it works
		// should get the name from the build maybe?  i don't know if it matters though
		clamwhoresJar = JJ.uri(com.clamwhores.assets.Index.class).resolve("../../../../clamwhores.jar");
	}

	
	@Test
	public void test() throws Exception {
		final Module underTest = new Module(clamwhoresJar, fssRule.threadPool());
		
		Thread.sleep(2000);
		
		assertTrue("underTest.inService()", underTest.inService());
	}
}
