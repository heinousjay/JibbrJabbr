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
import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotates a method be called with a {@link org.jsoup.select.Elements Elements}
 * collection that results from running the supplied selector against the active
 * document.
 * </p>
 * 
 * <p>
 * Examples:
 * </p>
 * <pre>
 * public void doSomething(@Select("div") Elements divs, @Select("body") Element body) {
 *   //do something interesting..
 * }
 *
 * @Select("*")
 * public void doSomethingElse(Elements all) {
 *   //do something else interesting..
 * }
 * </pre>
 * @author jason
 *
 */
@Retention(RUNTIME)
@Target({METHOD, PARAMETER})
@Documented
@Inherited
public @interface Select {
	String value();
}
