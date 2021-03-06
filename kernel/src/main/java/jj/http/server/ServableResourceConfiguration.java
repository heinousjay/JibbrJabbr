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
package jj.http.server;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jason
 *
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServableResourceConfiguration {

	/**
	 * The name by which this resource is exposed for routing. By default, will use
	 * the simple class name, with the word "Resource" removed from the end,
	 * and the initial letter lowercase - for example
	 * "jj.http.server.resource.StaticResource" will become "static"
	 * @return
	 */
	String name() default "";
	
	/**
	 * The {@link RouteProcessor} implementation that will process matching request.
	 * Defaults to {@link SimpleResourceServer}, which is configurable with {@link #processorConfig()}
	 * @return
	 */
	Class<? extends RouteProcessor> processor() default SimpleRouteProcessor.class;
	
	/**
	 * The {@link RouteProcessorConfiguration} implementation that configures the
	 * {@link RouteProcessor} supplied in {@link #processor()}, if needed and welcome
	 */
	Class<? extends RouteProcessorConfiguration> processorConfig() default DefaultRouteProcessorConfiguration.class;
	
	/**
	 * The {@link RouteContributor} implementation that will add {@link Route}s to the
	 * routing system
	 * @return
	 */
	Class<? extends RouteContributor> routeContributor() default EmptyRouteContributor.class;
}
