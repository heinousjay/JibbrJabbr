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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Formatter;

/**
 * <p>
 * Annotates relevant methods in Panel/Page objects and fields in Model objects
 * to facilitate location of a backing element. Only one attribute should be
 * used in a given declaration.  Doing otherwise is undefined and will probably
 * trip an assertion at some point.
 * 
 * <p>
 * The value attribute has special behavior - in nested contexts it will
 * concatenate with its parent's configured ID, should one exist.  An example
 * will make it clear.  Given the following declarations:
 * 
 * <pre>
 * {@literal @}{@link Model}
 * class Login {
 * 	// default By is assumed here as the field name, so nominally:
 * 	// {@literal @}By("username")
 * 	String username;
 * 	String password;
 * }
 * 
 * interface LoginPanel extends {@link Panel} {
 * 
 * 	{@literal @}By("login:")
 * 	LoginPanel setLoginInformation(Login login);
 * 
 * 	// other methods to do other things
 * }
 * 
 * interface IndexPage extends {@link Page} {
 * 
 * 	{@literal @}By("home:")
 * 	LoginPanel loginPanel();
 * 
 * 	// other methods to do other things
 * }
 * </pre>
 * 
 * then performing<pre>
 * Login login = new Login();
 * login.username = "username";
 * login.password = "password";
 * 
 * indexPage.loginPanel().setLoginInformation(login);
 * </pre>
 * 
 * would result in an attempt to set the value of the element
 * located at id = "home:login:username" to "username", and
 * similarly at "home:login:password" to "password".
 * 
 * <p>
 * Using the specific attributes is a direct setting, and the
 * ID hierarchy is no longer in effect.  That might change!
 * but it might not
 * 
 * <p>
 * By attributes can be defined using java format strings as detailed
 * in {@link Formatter}. This is managed by embedding the format flags
 * into the attribute, and defining the Panel method to take String and int
 * parameters that match.  The details for this are in implementation but
 * the basic idea is that the trailing parameters of the method (those not
 * used for method-specific implementation patterns) will be used to
 * satisfy the flags specified in the attribute, like so:
 * 
 *  <pre>
 *  interface IndexPage extends {@link Page} {
 * 
 * 	{@literal @}By("home:%s:")
 * 	LoginPanel loginPanel(String group);
 * 
 * }
 * </pre>
 * 
 * called like
 * 
 * <pre>
 * indexPage.loginPanel("group-name");
 * </pre>
 * 
 * will result in the parameter "group-name" being substituted into
 * the By value, which will then apply for concatenations onto By
 * values for the various methods in the Panel. Going with the previous
 * example:
 * 
 * <pre>
 * Login login = new Login();
 * login.username = "username";
 * login.password = "password";
 * 
 * indexPage.loginPanel("group-name").setLoginInformation(login);
 * </pre>
 * 
 * would result in an attempt to set the value of the element
 * located at id = "home:group-name:username" to "username", and
 * similarly at "home:group-name:password" to "password".
 * 
 * <p>
 * Combining these techniques gives a very flexible way to specify
 * methods for interacting with pages.
 * 
 * @author jason
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, METHOD})
public @interface By {

	/** the DOM ID as hier  */
	String value() default "";
	
	/** look up the element by its DOM ID  */
	String id() default "";
	
	/** look up the element by a matching class attribute.  only the first element found is used */
	String className() default "";
	
	/** 
	 * look up the element by a matching css selector.  you are responsible for ensuring this is only
	 * used with compatible drivers
	 */
	String cssSelector() default "";
	
	/** look up the element by an xpath expression */
	String xpath() default "";
}
