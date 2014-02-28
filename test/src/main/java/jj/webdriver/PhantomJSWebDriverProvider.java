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
package jj.webdriver;

import java.io.File;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * <p>
 * Provides a {@link PhantomJSDriver}.  Requires that the path to the phantomjs
 * binary be passed in as a system property under the key "phantomjs.binary.path"
 * 
 * <p>
 * This is probably your fastest bet for test running.
 * @author jason
 *
 */
public class PhantomJSWebDriverProvider implements WebDriverProvider {
	
	private static final String PHANTOMJS_BINARY_PATH_KEY = "phantomjs.binary.path";
	
	private final PhantomJSDriverService.Builder builder;
	
	PhantomJSWebDriverProvider() {
		String phantomJSPath = System.getProperty(PHANTOMJS_BINARY_PATH_KEY);
		
		assert phantomJSPath != null && !phantomJSPath.isEmpty() : 
			"cannot use " + 
			getClass().getSimpleName() + 
			" without providing a binary location as a system property at key \"" +
			PHANTOMJS_BINARY_PATH_KEY +
			"\"";

		File phantomJSBin = new File(phantomJSPath).getAbsoluteFile();
		
		builder = new PhantomJSDriverService.Builder()
			.usingPhantomJSExecutable(phantomJSBin)
			.usingAnyFreePort()
			.withLogFile(new File(phantomJSBin.getParentFile(), "phantomjs.log"));
	}

	@Override
	public WebDriver get() {
		return new PhantomJSDriver(builder.build(), DesiredCapabilities.phantomjs());
	}

}
