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

import javax.inject.Singleton;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Predicate;

/**
 * @author jason
 *
 */
@Singleton
public class ThreeSecondsAndDisplayedWebElementFinder implements WebElementFinder {

	@Override
	public WebElement find(final WebDriver webDriver, final By by) {
		
		try {
			new WebDriverWait(webDriver, 3).until(new Predicate<WebDriver>() {
				
				@Override
				public boolean apply(WebDriver webDriver) {
					WebElement webElement = webDriver.findElement(by);
					return webElement != null && webElement.isDisplayed();
				}
			});
		} catch (TimeoutException e) {
			throw new AssertionError("could not locate an element " + by);
		}
		
		return webDriver.findElement(by);
	}
	
}
