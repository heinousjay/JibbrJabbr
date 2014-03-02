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

/**
 * <p>
 * A specialized type of {@link Panel} that triggers/follows navigation, and is
 * intended to contain Panels as well as interaction method.
 * 
 * <p>
 * Extensions of this interface are required to be annotated with {@link URL}, which
 * should identify the path to which this page is associated.  query strings are
 * specified via the call to {@link WebDriverRule#get(Class, String)}.
 * 
 * <p>
 * When a page is created
 * 
 * @author jason
 *
 */
public interface Page extends Panel {
	
	String currentUrl();
}
