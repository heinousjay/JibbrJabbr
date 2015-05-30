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
 * implements the routing system.
 * 
 * <p>
 * during system configuration ({@link jj.configuration}):
 * <pre class="brush:js">
 * let { 
 *   route: route,
 *   redirect: redirect,
 *   welcomeFile: welcomeFile
 * } = require('uri-routing-configuration');
 * 
 * welcomeFile('index');
 * 
 * route.GET('/chat/').to('/chat/lobby');
 * route.GET('/chat/room/*name').to('/chat/room');
 * route.POST('/chat/upload').to('/chat/room');
 * </pre>
 * 
 * @author jason
 * 
 */
package jj.http.server.uri;