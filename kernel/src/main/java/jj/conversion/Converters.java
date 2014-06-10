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
package jj.conversion;

import static jj.util.CodeGenHelper.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * <p>
 * Provides simple object conversion services.  While it's written to be extensible,
 * and may well get extracted into an independent project, right now it's just
 * intended to support basic internal needs and as such isn't super flexible in the interface
 * </p>
 * 
 * <p>
 * Credit to guice et al for inspiring me as to the basic technique. 
 * </p>
 * @author jason
 *
 */
@Singleton
public class Converters {

	private final Map<Class<?>, Map<Class<?>, Converter<?, ?>>> converters = new HashMap<>();
	
	@Inject
	public Converters(final Set<Converter<?, ?>> converters) {
		for (Converter<?, ?> converter : converters) {
			register(converter);
		}
	}
	
	private void register(Converter<?, ?> converter) {
		
		Type[] interfaceTypes = converter.getClass().getGenericInterfaces();
		Type[] types = ((ParameterizedType)interfaceTypes[0]).getActualTypeArguments();
		
		Class<?> fromClass = (Class<?>)types[0];
		Class<?> fromPrimitiveClass = wrappersToPrimitives.get(fromClass);
		Class<?> toClass = (Class<?>)types[1];
		Class<?> toPrimitiveClass = wrappersToPrimitives.get(toClass);
		
		// first the basic
		add(fromClass, toClass, converter);
		// now check the wrappers
		if (fromPrimitiveClass != null) {
			add(fromPrimitiveClass, toClass, converter);
		}
		if (toPrimitiveClass != null) {
			add(fromClass, toPrimitiveClass, converter);
		}
		if (fromPrimitiveClass != null && toPrimitiveClass != null) {
			add(fromPrimitiveClass, toPrimitiveClass, converter);
		}
		
	}
	
	private void add(Class<?> fromClass, Class<?> toClass, Converter<?, ?> converter) {
		Map<Class<?>, Converter<?, ?>> fromConverters = converters.get(fromClass);
		if (fromConverters == null) {
			fromConverters = new HashMap<>();
			converters.put(fromClass, fromConverters);
		}
		converters.get(fromClass).put(toClass, converter);
	}
	
	// java makes you do weird shit to avoid warnings sometimes
	@SuppressWarnings("unchecked")
	private <To> To cast(Object in) {
		return (To)in;
	}
	
	/**
	 * <p>
	 * Converts an incoming value to the desire Class, according to
	 * the runtime type of the from parameter and the specified class
	 * of the to parameter.
	 * </p>
	 * 
	 * <p>Identity conversions always work, so converting without needing to is just fine</p>
	 * 
	 * <p>
	 * Throws {@link AssertionError}s if the type cannot be handled since
	 * it's currently configured programmatically
	 * </p>
	 * @param from The object being converted
	 * @param to The type to convert to
	 * @return
	 */
	public <From, To> To convert(From from, Class<To> to) {
		
		if (from == null) {
			if (primitiveDefaults.containsKey(to)) {
				return cast(primitiveDefaults.get(to));
			}
			return null;
		}
		
		Class<?> fromClass = from.getClass();
		
		if (to.isAssignableFrom(fromClass) ||
			to.isPrimitive() && primitivesToWrappers.get(to).isAssignableFrom(fromClass)) {
			return cast(from);
		}
		
		Map<Class<?>, Converter<?, ?>> fromConverters = converters.get(fromClass);
		
		if (fromConverters == null && to == String.class) {
			return cast(String.valueOf(from));
		}
		
		assert (fromConverters != null) : "don't know how to convert from " + fromClass + " to " + to;
		
		@SuppressWarnings("unchecked")
		Converter<From, To> converter = (Converter<From, To>)fromConverters.get(to);
		
		if (converter == null && to == String.class) {
			return cast(String.valueOf(from));
		}
		
		
		assert (converter != null) : "don't know how to convert " + fromClass + " to " + to;
		
		Object value = converter.convert(from);
		
		To result = cast(primitivesToWrappers.containsKey(to) ? primitivesToWrappers.get(to).cast(value) : value);
		return result;
	}
	
	@Override
	public String toString() {
		return converters.toString();
	}
	
}
