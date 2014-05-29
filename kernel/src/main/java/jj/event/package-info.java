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
 * An event bus for the system.
 * 
 * <p>
 * Subscribing to events works as part of the injection process, so only objects created
 * by Guice can participate. First, annotate the class with {@link Subscriber}, then
 * declare an instance method with any name, of any access except private, returning
 * nothing or anything at all (the value is ignored) and that takes a single parameter of
 * the event type you wish to receive.  Annotate this method with {@link Listener}.
 * Congratulations! Instances of this class will now receive these events.  You can, of
 * course, declare as many listeners as you wish.
 * 
 * <p>
 * Events are delivered strictly by type, so you may need to inspect incoming events
 * to see if you care.
 * 
 * <p>
 * You can inject {@link Publisher} to publish events to all registered listeners.
 * 
 * <p>
 * Listeners cannot be unregistered, so instances will receive events
 * across their entire lifetimes.
 * 
 * <p>
 * No serious processing should be done in event listeners since you have no control over
 * what thread is running - start a task instead! Also, throwing anything from a listener
 * method is considered a programming error, and it will cause assertion errors to be
 * thrown into unspecified parts of the system, so don't do it!  The easiest way to comply
 * with this advice is to inject the {@link jj.execution.TaskRunner} and do something like:
 * <pre class="brush:java">
 * {@literal @}Listener
 * void event(final Event event) {
 *   taskRunner.execute(new ServerTask("processing event") { // or some other task type
 *     public void run() throws Exception {
 *       doSomethingInterestingWithThe(event);
 *     }
 *   });
 * }
 * </pre>
 * 
 * @author jason
 *
 */
package jj.event;