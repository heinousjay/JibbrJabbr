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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

import com.google.common.base.Predicate;

/**
 * waits three seconds for the element to exist and be displayed.  if the
 * element is not found, a warning is logged, and it waits three more
 * seconds, then bails
 * 
 * @author jason
 *
 */
@Singleton
public class ImpatientWebElementFinder implements WebElementFinder {

	private final Logger logger;
	
	@Inject
	ImpatientWebElementFinder(final Logger logger) {
		this.logger = logger;
	}
	
	@Override
	public WebElement find(final WebDriver webDriver, final By by) {
		int tries = 1;
		do {
			try {
				new WebDriverWait(webDriver, 3).until(new Predicate<WebDriver>() {
					
					@Override
					public boolean apply(WebDriver webDriver) {
						WebElement webElement = webDriver.findElement(by);
						return webElement != null && webElement.isDisplayed();
					}
				});
				// if we succeed, exit now
				tries = 0;
			
			} catch (TimeoutException e) {
				if (tries == 0) {
					throw new AssertionError("gave up locating an element after 6 seconds " + by);
				} else {
					logger.warn("can't find element {} in 3 seconds. trying three more.", by);
				}
			}

		} while (tries-- > 0);
		
		return webDriver.findElement(by);
	}
}
