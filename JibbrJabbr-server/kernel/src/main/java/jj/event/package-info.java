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
 * The server's event bus. Annotate classes with {@link Subscriber} to indicate that
 * instances will listen to events.  Annotate event listening methods with {@link Listener}.
 * The methods should take a single parameter, which is the event.  Publishing is done by
 * injecting the Publisher.  Events cannot be unregistered, so instances will receive events
 * across their entire lifetimes.  References are held weakly to prevent memory issues.
 * No serious processing should be done in event listeners, dispatch tasks to do any
 * work.
 * 
 * @author jason
 *
 */
package jj.event;