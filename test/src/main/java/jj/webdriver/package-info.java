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
 * Provides test facilities to drive in-browser testing of web apps
 * 
 * <p>
 * The main entry point to this API is {@link WebDriverRule}
 * 
 * <p>
 * Customizing the behavior involves adding {@link PanelMethodGenerator}
 * classes to the generation system, and potentially extending {@link PanelBase}
 * with additional base functionality.
 * 
 * @author jason
 *
 */
package jj.webdriver;
import jj.webdriver.panel.PanelBase;
import jj.webdriver.panel.PanelMethodGenerator;
