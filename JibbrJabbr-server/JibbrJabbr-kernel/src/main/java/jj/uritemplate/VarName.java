/*
 * adapted from https://github.com/damnhandy/Handy-URI-Templates
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
package jj.uritemplate;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
* When this annotation is placed on a field or getter method, the
* annotation value will be used instead of the property name.
*
* @author <a href="ryan@damnhandy.com">Ryan J. McDonough</a>
* @version $Revision: 1.1 $
* @since 1.0
*/
@Documented
@Retention(RUNTIME)
@Target({FIELD, METHOD})
public @interface VarName {

   /**
* Returns the preferred name of the property.
*
* @return
*/
   String value();
}