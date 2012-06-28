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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Documents that a method can potentially block, and so should never be called using
 * the asynchronous executor.  Methods decorated as Blocking can safely call all methods,
 * and cannot be called by methods marked NonBlocking.  Classes that bear methods marked
 * Blocking in their public API should provide equivalent NonBlocking methods.  Constructors
 * that are blocking simply should never be called in a nonblocking context.
 * </p>
 * 
 * <p>
 * We'll retain this at runtime in case we can figure out a smart way to make
 * it meaningful, but for now it's documentation
 * </p>
 * 
 * @author jason
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Documented
@Inherited
public @interface Blocking {

}
