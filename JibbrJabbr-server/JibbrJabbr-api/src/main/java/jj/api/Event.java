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
package jj.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a type as being an event.  Instances of that type can automatically participate in
 * event mediation.  Any component with a public void method of any name that takes a single
 * parameter of an event type will be registered as a listener, and whenever an event is published,
 * that method will be invoked.
 * 
 * use the {@link Blocking}/{@link NonBlocking} annotations to indicate if the event listener method might block. if
 * no annotation is supplied, it will be assumed that blocking is possible.  at this point it is not
 * clear what impacts this will have but it is likely that nonblocking methods will execute more quickly
 * 
 * static methods will be ignored.
 * 
 * event objects should almost certainly be immutable.  this may be enforced
 * 
 * @author jason
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
@Inherited
public @interface Event {}
