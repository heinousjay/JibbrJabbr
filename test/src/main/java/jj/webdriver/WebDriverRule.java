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
import java.io.IOException;
import javax.inject.Singleton;

import jj.webdriver.URLBase.BaseURL;

import org.apache.commons.codec.binary.Base64;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.ScreenshotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * <p>
 * Manages a WebDriver to connect to a configured browser instance,
 * producing Page/Panel instances to drive the browser for testing
 * purposes
 * 
 * <p>
 * Usage example:<pre>
 * 
 * public class SomeBrowserDrivenTest {
 * 
 * 	{@literal @}{@link Rule}
 * 	public WebDriverRule webDriverRule = new WebDriverRule()
 * 		.baseUrl(... project specific configuration ...)
 * 		.driverProvider(... defaults to {@link PhantomJSWebDriverProvider} ...);
 * 
 * 	{@literal @}{@link Test}
 * 	public void test() {
 * 		SomePage page = webDriverRule.get(SomePage.class);
 * 		// drive the page!
 * 	}
 * }
 * 
 * @author jason
 *
 */
public class WebDriverRule implements TestRule {
	
	private static final String SEPARATOR = "*************************************************************************************";
	
	// TODO is it reasonable even having a default here?
	private String baseUrl = "http://localhost:8080";
	
	private Class<? extends WebDriverProvider> webDriverProvider = null;
	
	private Class<? extends WebElementFinder> webElementFinder = ThreeSecondsAndDisplayedWebElementFinder.class;
	
	private Class<? extends PanelBase> panelBaseClass = PanelBase.class;
	
	private Description currentDescription = null;
	
	private int screenShotCount;
	
	private Logger logger = null;
	
	private Injector injector = null;
	
	private WebDriver webDriver = null;
	
	@Override
	public Statement apply(final Statement base, final Description description) {
		
		return new Statement() {
			
			@Override
			public void evaluate() throws Throwable {
				
				assert webDriverProvider != null : "you must supply a WebDriverProvider";
				
				currentDescription = description;
				
				logger = LoggerFactory.getLogger("test runner");
				
				injector = Guice.createInjector(
					new AbstractModule() {
						
						@Override
						protected void configure() {
							bind(new TypeLiteral<Class<? extends PanelBase>>() {}).toInstance(panelBaseClass);
							bind(String.class).annotatedWith(BaseURL.class).toInstance(baseUrl);
							bind(WebDriver.class).toProvider(webDriverProvider).in(Singleton.class);
							bind(WebElementFinder.class).to(webElementFinder);
							bind(Logger.class).toInstance(logger);
						}
					},
					new PanelMethodGeneratorsModule()
				);
				
				webDriver = injector.getInstance(WebDriver.class);
				
				try {
					logger.info(SEPARATOR);
					logger.info("beginning {}.{}", description.getClassName(), description.getMethodName());
					logger.info("using driver {}", webDriver);
					base.evaluate();
				} catch (Throwable t) {	
					
					logger.error("TEST ENDED IN ERROR", t);
					
					if (!saveScreenshotIfFound(t)) {
						takeScreenshot(null, makeScreenShotName("error-screenshot"));
					}
					
					throw t;
					
				} finally {
					
					logger.info(SEPARATOR + "\n");
					webDriver.quit();
					currentDescription = null;
					webDriver = null;
					injector = null;
					logger = null;
					screenShotCount = 0;
				}
			}
		};
	}
	
	private boolean saveScreenshotIfFound(Throwable t) {
		boolean hasScreenshot = false;
		if (t.getCause() instanceof ScreenshotException) {
			
			try {
				hasScreenshot = true;
				String screenshotBase64 = ((ScreenshotException)t.getCause()).getBase64EncodedScreenshot();
				
				byte[] screenshot = Base64.decodeBase64(screenshotBase64);
				
				File screenshotFile = new File(makeScreenShotName("error-screenshot")).getAbsoluteFile();
				
				Files.write(screenshot, screenshotFile);
				logger.info("saved error state screenshot {}", screenshotFile);
			
			} catch (Exception ioe) {
				logger.error("couldn't save the error screenshot", ioe);
			}
		}
		
		return hasScreenshot;
	}
	
	private void assertUnstarted() {
		assert currentDescription == null : "rule configuration must be before test runs begin";
	}
	
	/**
	 * <p>
	 * Configure the base URL for the test run.  URLs are determined with simple
	 * concatenation - the URL configured for a Page interface is appended to
	 * the value configured here. Default is "http://localhost:8080"
	 * @param baseUrl
	 * @return
	 */
	public WebDriverRule baseUrl(final String baseUrl) {
		assertUnstarted();
		
		this.baseUrl = baseUrl;
		return this;
	}
	
	/**
	 * <p>
	 * Configure the class that provides the {@link WebDriver} implementation for the 
	 * test run.  Default is {@link PhantomJSWebDriverProvider}.
	 * 
	 * <p>
	 * The provider will be bound as a singleton
	 * 
	 * @param webDriverProvider
	 * @return
	 */
	public WebDriverRule driverProvider(Class<? extends WebDriverProvider> webDriverProvider) {
		assertUnstarted();
		
		this.webDriverProvider = webDriverProvider;
		return this;
	}
	
	public WebDriverRule webElementFinder(Class<? extends WebElementFinder> webElementFinder) {
		assertUnstarted();
		
		this.webElementFinder = webElementFinder;
		return this;
	}
	
	public WebDriverRule panelBaseClass(Class<? extends PanelBase> panelBaseClass) {
		assertUnstarted();
		
		this.panelBaseClass = panelBaseClass;
		return this;
	}
	
	/**
	 * Takes a screenshot of the current state of the browser, if possible according to the
	 * current driver, and stores it in the current directory, which is dependent upon
	 * test invocation
	 * @param dir
	 * @throws IOException
	 */
	public void takeScreenshot() throws IOException {
		takeScreenshot(null);
	}
	
	/**
	 * Takes a screenshot of the current state of the browser, if possible according to the
	 * current driver, and stores it in the directory given
	 * @param dir
	 * @throws IOException
	 */
	public void takeScreenshot(File dir) throws IOException {
		takeScreenshot(dir, makeScreenShotName("screenshot"));
	}
	
	/**
	 * Takes a screenshot of the current state of the browser, if possible according to the
	 * current driver, and stores it in the directory given
	 * @param dir
	 * @throws IOException
	 */
	public void takeScreenshot(File dir, String screenshotName) throws IOException {
		
		assert webDriver != null : "cannot take a screenshot outside of a test";
		assert dir == null || dir.isDirectory() : "need a directory to store the screenshot";
		
		if (webDriver instanceof TakesScreenshot) {
		
			File file = ((TakesScreenshot)webDriver).getScreenshotAs(OutputType.FILE);
			
			Files.copy(file, new File(dir, screenshotName));
			
			logger.info("saved {}", screenshotName);
		}
	}

	private String makeScreenShotName(String base) {
		return String.format("%s-%d-%s.%s.png",
			base,
			++screenShotCount,
			currentDescription.getClassName(),
			currentDescription.getMethodName()
		);
	}
	
	public <T extends Page> T get(final Class<T> pageInterface) {
		
		assert webDriver != null : "cannot get a page outside of a test";
		
		assert pageInterface.getAnnotation(URL.class) != null : "page declarations must have a URL annotation";
		
		webDriver.get(baseUrl + pageInterface.getAnnotation(URL.class).value());
		
		return injector.getInstance(PanelFactory.class).create(pageInterface);
	}
}
