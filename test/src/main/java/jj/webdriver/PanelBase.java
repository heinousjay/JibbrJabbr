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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;

/**
 * <h1>DO NOT EXTEND THIS DIRECTLY TO CREATE PANEL/PAGE OBJECTS</h1>
 * 
 * <p>Go look at {@link Panel}
 * 
 * <p>
 * provides basic services for generated page and panel implementations
 * 
 * <p>
 * throws in pretty much every situation when something goes wrong,
 * because that's a test failure, generally speaking
 * 
 * <p>
 * You can extend this class to provide additional base functionality for
 * customer generators if necessary.  For the time, this class is package
 * private, which is intended to make you think about using it for anything
 * 
 * 
 * @author jason
 *
 */
abstract class PanelBase implements Page {
	
	protected final WebDriver webDriver;
	protected final WebElementFinder finder;
	protected final Logger logger;
	protected final String name;
	protected final PanelFactory panelFactory;
	protected final URLBase urlBase;

	PanelBase(
		final WebDriver webDriver,
		final WebElementFinder finder,
		final PanelFactory panelFactory,
		final Logger logger,
		final URLBase urlBase
	) {
		this.webDriver = webDriver;
		this.finder = finder;
		this.panelFactory = panelFactory;
		this.logger = logger;
		this.name = getClass().getInterfaces()[0].getName();
		this.urlBase = urlBase;
		
		logger.info("{} created. url is {}", name, currentUrl());
	}
	
	private void log(String action, By by) {
		logger.info("{} {} {}", name, by, action);
	}
	
	private WebElement find(By by) {
		
		return finder.find(webDriver, by);
	}
	
	<T extends Panel> T makePanel(Class<T> panelInterface) {
		return panelFactory.create(panelInterface);
	}
	
	<T extends Page> T navigateTo(Class<T> pageInterface) {
		String url = pageInterface.getAnnotation(URL.class).value();
		
		// this is nowhere near right!
		if (!webDriver.getCurrentUrl().endsWith(url)) {
			// we need to navigate there!
			webDriver.get(urlBase.resolve(url));
		}
		
		return panelFactory.create(pageInterface);
	}
	
	void click(By by) {
		log("click", by);
		find(by).click();
	}
	
	void set(By by, String value) {
		log("set " + value, by);
		find(by).sendKeys(value);
	}
	
	@Override
	public final String currentUrl() {
		return webDriver.getCurrentUrl();
	}
	
	WebDriver webDriver() {
		return webDriver;
	}
}
