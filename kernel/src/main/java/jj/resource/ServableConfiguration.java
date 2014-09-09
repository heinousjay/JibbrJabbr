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
package jj.resource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jj.http.server.SimpleRouteProcessor;
import jj.http.uri.RouteProcessor;

/**
 * @author jason
 *
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServableConfiguration {

	/**
	 * The name by which this resource is exposed for routing. By default, will use
	 * the simple class name, with the word "Resource" removed from the end,
	 * and the initial letter lowercase - for example
	 * "jj.resource.stat.ic.StaticResource" will become "static"
	 * @return
	 */
	String name() default "";
	
	/**
	 * The {@link ResourceServer} implementation that will process matching request.
	 * Defaults to {@link SimpleResourceServer} 
	 * @return
	 */
	Class<? extends RouteProcessor> processor() default SimpleRouteProcessor.class;
}
