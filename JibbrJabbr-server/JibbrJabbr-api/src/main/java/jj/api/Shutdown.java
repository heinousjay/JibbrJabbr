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

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotates a method that should be called to signal shutdown,
 * which occurs some time after startup. This is an opportunity
 * to clean up all resources acquired. Once the method completes,
 * the object should be ready to be started again.
 * </p>
 * 
 * <p>
 * The method may be called at additional points over the lifetime of an object,
 * after a method annotated with {@link Startup} (if it is defined) has been
 * called. (In general, if you define a Startup, you should define a Shutdown)
 * <p>
 * 
 * <p>
 * The lifecycle is
 * <ul>
 * 	<li>Initialization (construction)</li>
 * 	<li>Startup</li>
 * 	<li>Shutdown</li>
 * 	<li>(Startup)</li>
 * 	<li>(Shutdown)...</li>
 * 	<li>{@link Dispose}</li>
 * 	<li>{@link Object#finalize()} (which obviously, avoid)</li>
 * </ul>
 * <p>
 * @author jason
 *
 */
@Retention(RUNTIME)
@Target(METHOD)
@Documented
@Inherited
public @interface Shutdown {

}
