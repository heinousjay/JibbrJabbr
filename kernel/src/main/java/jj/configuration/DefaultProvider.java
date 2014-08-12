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
package jj.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import javax.inject.Provider;

/**
 * <p>
 * Designates an implementation of {@link Provider} that will
 * provide a default value for a complicated configuration property.
 * 
 * <p>
 * Unfortunately the type system won't let me specify the types, so it's
 * a runtime check, but it goes without saying that the implementation
 * should provide the return type of the method. But I said it anyway.
 * 
 * @author jason
 *
 */
@Target(ElementType.METHOD)
@Documented
public @interface DefaultProvider {
	
	Class<? extends Provider<?>> value();
}
