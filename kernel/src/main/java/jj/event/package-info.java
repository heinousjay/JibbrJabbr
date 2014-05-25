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
 * The server's event bus. 
 * 
 * <p>
 * Annotate classes with {@link Subscriber} to indicate that instances will listen to
 * events.  Annotate event listening methods with {@link Listener}. Listener methods
 * must take a single parameter, which is the event.  Congratulations! You
 * will now receive those events.
 * 
 * <p>
 * You can inject {@link Publisher} to publish events to all registered listeners.
 * 
 * <p>
 * Events cannot be unregistered, so instances will receive events
 * across their entire lifetimes.  References are held weakly to prevent memory issues.
 * 
 * <p>
 * No serious processing should be done in event listeners since you have no control over
 * what thread is running - start a task instead! Also, throwing anything from a listener
 * method is considered a programming error, and it will cause assertion errors to be
 * thrown into unspecified parts of the system, so don't do it!
 * 
 * @author jason
 *
 */
package jj.event;