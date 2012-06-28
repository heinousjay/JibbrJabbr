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
 * Documents that a method will never block, and so is suitable for use in
 * the asynchronous executor.  Methods decorated with this annotation must never
 * call methods decorated with {@link Blocking}.  Note that all logging is currently
 * potentially blocking, so there can be no logging from a method decorated
 * with this annotation.
 * </p>
 * 
 * <p>
 * Watch out for hidden sources of blocking, there are still a few in here for
 * instance.  Things like querying the file system could block arbitrarily.
 * Transient locking around putting tasks on queues should be okay, which is
 * good, because that's basically unavoidable.
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
public @interface NonBlocking {

}
