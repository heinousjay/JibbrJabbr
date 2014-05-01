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
package jj.webdriver.panel;

import jj.webdriver.WebElementFinder;
import jj.webdriver.panel.PanelBase;
import jj.webdriver.panel.PanelFactory;
import jj.webdriver.panel.URLBase;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

/**
 * @author jason
 *
 */
public class PointlessPanelBase extends PanelBase {

	/**
	 * yeah, ugly but the IDE can generate it for you.
	 * 
	 * @param webDriver
	 * @param finder
	 * @param panelFactory
	 * @param logger
	 * @param urlBase
	 */
	public PointlessPanelBase(WebDriver webDriver, WebElementFinder finder, PanelFactory panelFactory, Logger logger, URLBase urlBase) {
		super(webDriver, finder, panelFactory, logger, urlBase);
	}

}
