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
package jj.webtest;

import java.io.File;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * supplies a configured webdriver
 * 
 * @author jason
 *
 */
public class WebDriverRule implements TestRule {
	
	private static final String PHANTOMJS_BINARY_PATH_KEY = "phantomjs.binary.path";
	
	private static final String PHANTOMJS_LOCAl_STORAGE_PATH_ARG_FORMAT = "--local-storage-path=%s";
	
	private static final PhantomJSDriverService.Builder builder;
	
	static {
		File phantomJSBin = new File(System.getProperty(PHANTOMJS_BINARY_PATH_KEY)).getAbsoluteFile();
		
		builder = new PhantomJSDriverService.Builder()
			.usingPhantomJSExecutable(phantomJSBin)
			.usingCommandLineArguments(new String[] {
				String.format(PHANTOMJS_LOCAl_STORAGE_PATH_ARG_FORMAT, phantomJSBin.getParent())
			})
			.usingAnyFreePort()
			.withLogFile(new File(phantomJSBin.getParentFile(), "phantomjs.log"));
	}
	
	private PhantomJSDriver current = null;
	
	public WebDriverRule() {
		System.err.println("CREATED BITCHES");
	}
	
	@Override
	public Statement apply(final Statement base, final Description description) {
		
		return new Statement() {
			
			@Override
			public void evaluate() throws Throwable {
				try {
					base.evaluate();
				} finally {
					if (current != null) {
						current.quit();
						//current = null;
					}
				}
			}
		};
	}

	public WebDriver get(final String url) {
		
		assert (current == null) : "a driver is already in use. doesn't support multiples yet, but soon";
		
		current = new PhantomJSDriver(builder.build(), DesiredCapabilities.phantomjs());
		
		current.get(url);
		
		return current;
	}
}
