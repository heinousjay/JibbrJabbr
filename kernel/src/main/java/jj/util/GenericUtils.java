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
package jj.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Some methods for dealing with generic parameters, like the
 * TypeLiteral from Guice, more or less
 * 
 * @author jason
 *
 */
public enum GenericUtils {
	
	;
	
	public static Class<?> extractGenericParameter(Class<?> target) {
		return extractGenericParameter(target, 0);
	}
	
	public static Class<?> extractGenericParameter(Class<?> target, int index) {
		
		Type superClass = target.getGenericSuperclass();
		
		assert !(superClass instanceof Class) : "doesn't have a generic superclass";
		
		ParameterizedType parameterized = (ParameterizedType)superClass;
		Type parameter = parameterized.getActualTypeArguments()[index];
		
		assert parameter instanceof Class : "parameter at index " + index + " is not fully specified";
		
		return (Class<?>)parameter;
	}
}