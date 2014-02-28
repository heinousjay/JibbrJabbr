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
package jj.webdriver;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Marks a class as representing a model, which is a unit of interaction
 * with form in a page object.  Models are simple bags of String fields,
 * any access except private, and not final, annotated with information 
 * to locate their backing elements in the document if necessary.
 * 
 * <p>
 * Model declarations should be of the form<pre>
 * 
 * {@literal @}Model
 * class UserRegistration {
 * 
 * 	// the model fields are assumed to be {@literal @}{@link By}(fieldName)
 *	// so here it's {@literal @}By("name")
 * 	String name;
 * 
 * 	String password;
 * 
 * 	// camelCase names are lowercased and dashed, so
 * 	// {@literal @}By("confirm-password")
 * 	String confirmPassword;
 * 
 * 	// and if you have to, just annotate it.
 * 	{@literal @}By(id = "first-in-it")
 * 	String weirdlyMismatched;
 * 
 * }
 * </pre>
 * 
 * 
 * <p>
 * TODO - once it handles forms, this also becomes the interface for looking up text/attributes from the document
 * 
 * @author jason
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
@Documented
public @interface Model {

}
