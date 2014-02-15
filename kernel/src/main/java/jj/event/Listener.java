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
package jj.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * <p>
 * Marks a method as an event listener on all injector-created instances
 * marked as {@link Subscriber}
 * 
 * <p>
 * The method
 * must take a single parameter, which is the event
 * type. The listener can be any access except private.
 * It can return anything at all (including void),
 * but the event system will disregard it.
 * 
 * <p>
 * Events can be any type, or any interface,
 * with the exception of {@link Object}. Event
 * delivery respects the class hierarchy, so subscribing
 * to a base event will get delivery for all sub types.
 * Due to type erasure, generic classes
 * do not work.
 * 
 * @author jason
 *
 */
@Target(ElementType.METHOD)
@Documented
public @interface Listener {

}
