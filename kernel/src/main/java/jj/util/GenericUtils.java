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
import java.lang.reflect.TypeVariable;

/**
 * Some methods for dealing with generic parameters, like the
 * TypeLiteral from Guice, more or less
 * 
 * @author jason
 *
 */
public enum GenericUtils {
	
	;

	public static Type extractTypeParameter(Class<?> subclass, Class<?> superclass, String typeVariableName) {
		
		assert subclass != null : "null subclass";
		assert superclass != null : "null superclass";
		assert superclass != Object.class : "superclass cannot be Object";
		assert !superclass.isInterface() : "superclass must be a class, not an interface. for now anyway";
		
		int index = findTypeVariableIndex(superclass, typeVariableName);
		
		assert (index < superclass.getTypeParameters().length) : "couldn't find the type variable";
		
		return extractTypeParameter(subclass, superclass, index);
	}
	
	public static Class<?> extractTypeParameterAsClass(Class<?> subclass, Class<?> superclass, String typeVariableName) {
		Type result = extractTypeParameter(subclass, superclass, typeVariableName);
		assert result instanceof Class : "parameter at index is not fully specified " + result;
		return (Class<?>)result;
	}

	private static int findTypeVariableIndex(Class<?> superclass, String typeVariableName) {
		int index = 0;
		for (TypeVariable<?> tv : superclass.getTypeParameters()) {
			if (typeVariableName.equals(tv.getName())) { break; }
			index++;
		}
		return index;
	}
	
	/**
	 * returns the {@link Type} found at a given index of type parameters for a parameterized
	 * superclass of a given subclass. Does not yet handle interfaces.
	 * 
	 * @param subclass The leaf class that extends some generic superclass
	 * @param superclass The extended class expecting reified type parameters
	 * @param index The index of the type parameter in the superclass
	 * @return
	 */
	private static Type extractTypeParameter(Class<?> subclass, Class<?> superclass, int index) {
		
		Class<?> current = subclass;
		while (current.getSuperclass() != superclass && current.getSuperclass() != Object.class) {
			current = current.getSuperclass();
		}
		
		assert current.getSuperclass() == superclass : "couldn't find target in hierarchy";

		
		Type targetType = current.getGenericSuperclass();

		assert targetType instanceof ParameterizedType : "doesn't have a generic superclass";
		
		ParameterizedType parameterized = (ParameterizedType)targetType;
		
		assert parameterized.getActualTypeArguments().length > index : "not enough type parameters in target for index";
		
		Type result = parameterized.getActualTypeArguments()[index];
		
		return result instanceof Class ? result : extractTypeParameter(subclass, current, result.toString());
	}
}