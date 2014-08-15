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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;

/**
 * @author jason
 *
 */
public class ResourceBinder {

	private final MapBinder<Class<? extends AbstractResource>, SimpleResourceCreator<? extends AbstractResource>> resourceCreatorBinder;
	
	private final MapBinder<Pattern, Class<? extends ServableResource>> servableResourceBinder;
	
	public ResourceBinder(final Binder binder) {
		resourceCreatorBinder = MapBinder.newMapBinder(
			binder,
			new TypeLiteral<Class<? extends AbstractResource>>() {},
			new TypeLiteral<SimpleResourceCreator<? extends AbstractResource>>() {}
		);
		
		servableResourceBinder = MapBinder.newMapBinder(
			binder,
			new TypeLiteral<Pattern>() {},
			new TypeLiteral<Class<? extends ServableResource>>() {}
		);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractResource, U extends SimpleResourceCreator<T>> LinkedBindingBuilder<U> of(Class<T> key) {
		
		if (ServableResource.class.isAssignableFrom(key)) {
			assert key.isAnnotationPresent(PathPattern.class) : key.getName() + " requires " + PathPattern.class.getSimpleName() + " annotation";
			try {
				Pattern pattern = Pattern.compile(key.getAnnotation(PathPattern.class).value());
				servableResourceBinder.addBinding(pattern).toInstance((Class<? extends ServableResource>)key);
			} catch (PatternSyntaxException pse) {
				throw new AssertionError(key.getName() + " " + PathPattern.class.getSimpleName() + " annotation has invalid pattern", pse);
			}
		}
		
		return (LinkedBindingBuilder<U>)resourceCreatorBinder.addBinding(key);
	}
}
