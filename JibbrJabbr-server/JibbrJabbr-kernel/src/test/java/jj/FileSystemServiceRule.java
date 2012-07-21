package jj;

import static java.util.concurrent.TimeUnit.SECONDS;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import jj.io.FileSystemService;
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

/**
 * <p>
 * Test rule to scaffold the file system service for things that are dependent on this.
 * </p>
 * 
 * <p>
 * Include in your test class like
 * <pre>@Rule public FileSystemServiceRule fssRule = new FileSystemServiceRule();</pre>
 * and then you will have a running instance of the file system service during your test.
 * </p>
 * 
 * 
 * @author jason
 *
 */
public class FileSystemServiceRule extends CoreResourcesRule {

	
	private FileSystemService fss;
	
	/**
	 * probably belongs in the core... except we don't want to start it all the time.
	 * 
	 * gotta think this through.  maybe a gl
	 */
	private MockThreadPool threadPool;
	
	public FileSystemService fileSystemService() {
		return fss;
	}
	
	public MockThreadPool threadPool() {
		return threadPool;
	}
	
	@Override
	public Statement apply(final Statement base, final Description description) {
		
		return new Statement() {
			
			@Override
			public void evaluate() throws Throwable {
				
				threadPool = new MockThreadPool();
				fss = new FileSystemService(threadPool, logger(), messageConveyor());
				
				base.evaluate();
				
				fss.control(KernelControl.Dispose);
				
				threadPool.shutdown();
				threadPool.awaitTermination(10, SECONDS);
				threadPool = null;
				
				fss = null;
				
				// just clear it out.
				mockLogger().messages();
				
				/* no need to print this stuff, really
				for (LogBundle lb : mockLogger().messages()) {
					System.out.println(lb);
				}
				*/
			}
		};
	}
}
