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
package com.clamwhores.assets;

import jj.api.html.Element;
import jj.api.html.Select;

/**
 * @author jason
 *
 */
public class Index {

	public void setProfileLink(@Select("a#profile-link") Element link) {
		link.attr("href", "/profile/heinousjay").text("heinousjay");
	}
}