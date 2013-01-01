/*
 * adapted from https://github.com/damnhandy/Handy-URI-Templates
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
package jj.uri;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jason
 *
 */
class VarExploder {
	
	/**
	 * The original object.
	 */
	private Object source;

	/**
	 * The objects properties that have been extracted to a {@link Map}
	 */
	private Map<String, Object> pairs = new LinkedHashMap<>();

	/**
	 * 
	 * @param source the Object to explode
	 */
	public VarExploder(Object source) {
		this.setSource(source);
	}

	public Map<String, Object> getNameValuePairs() {
		return pairs;
	}

	public void setSource(Object source) {
		this.source = source;
		this.initValues();
	}

	private void initValues() {

		Class<?> c = source.getClass();
		if (c.isAnnotation() || c.isArray() || c.isEnum() || c.isPrimitive()) {
			throw new IllegalArgumentException("The value must an object");
		}
		BeanInfo beanInfo;
		try {
			beanInfo = Introspector.getBeanInfo(c);
		} catch (IntrospectionException e) {
			throw new VariableExpansionException(e);
		}
		for (PropertyDescriptor p : beanInfo.getPropertyDescriptors()) {
			Method readMethod = p.getReadMethod();
			if (!readMethod.isAnnotationPresent(UriTransient.class) && !p.getName().equals("class")) {
				Object value = getValue(readMethod);
				String name = p.getName();
				if (readMethod.isAnnotationPresent(VarName.class)) {
					name = readMethod.getAnnotation(VarName.class).value();
				}
				if (value != null) {
					pairs.put(name, value);
				}
			}

		}
		scanFields(c);
	}

	/**
	 * Scans the fields on the class or super classes to look for annotations on
	 * the fields.
	 * 
	 * @param c
	 */
	private void scanFields(Class<?> c) {
		
		if (!c.isInterface()) {
			Field[] fields = c.getDeclaredFields();
			for (Field field : fields) {
				String fieldName = field.getName();

				if (pairs.containsKey(fieldName)) {
					if (field.isAnnotationPresent(UriTransient.class)) {
						pairs.remove(fieldName);
					} else if (field.isAnnotationPresent(VarName.class)) {
						String name = field.getAnnotation(VarName.class)
								.value();
						pairs.put(name, pairs.get(fieldName));
						pairs.remove(fieldName);
					}
				}
			}
		}
		
		/*
		 * We still need to scan the fields of the super class if its not Object
		 * to check for annotations. There might be a better way to do this.
		 */
		if (!c.getSuperclass().equals(Object.class)) {
			scanFields(c.getSuperclass());
		}
	}

	private Object getValue(Method method) {
		try {
			if (method == null) {
				return null;
			}
			return method.invoke(source);
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			throw new VariableExpansionException(e);
		} 
	}

	public Collection<Object> getValues() {
		Collection<Object> c = pairs.values();
		return c;
	}

	}