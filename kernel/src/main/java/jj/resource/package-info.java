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
 * Provides the abstraction of managing server resources, which are basically units
 * of computation for the JibbrJabbr system.
 * 
 * <p>
 * At the most basic level, a {@link jj.resource.FileResource} represents a generic file in
 * the file system. specific implementations provide the details
 * 
 * <p>
 * At a more complicated level, script execution environments are composed of several
 * sub-resources used together to produce output in response to user requests.
 * 
 * <p>
 * The resource system keeps itself up-to-date with the filesystem in an unobtrusive,
 * constant manner, unless this is disabled by passing an argument of <pre>fileWatcher=false</pre>
 * on the command line
 * 
 * <p>
 * There are two main components that act as the API to the resource system, the {@link ResourceFinder}
 * and the {@link jj.resource.ResourceLoader}.  The system also produces several events (which can be observed by
 * registering as described in {@link jj.event.Listener}) describing resource lifecycles, all descended from
 * {@link jj.resource.ResourceEvent}
 * 
 * <ul>
 * <li>{@link jj.resource.ResourceLoaded} when a resource is loaded
 * <li>{@link jj.resource.ResourceNotFound} when a resource was requested but not found
 * <li>{@link jj.resource.ResourceReloaded} when a resource is being reloaded
 * <li>{@link jj.resource.ResourceError} when an error occurs loading a resource
 * <li>{@link jj.resource.ResourceKilled} when a resource is removed from service
 * </ul>
 * 
 * @author jason
 *
 */
package jj.resource;

