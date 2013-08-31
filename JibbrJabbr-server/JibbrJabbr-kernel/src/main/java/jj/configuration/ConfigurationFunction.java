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

import java.util.ArrayList;
import java.util.Map;

import jj.conversion.Converters;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

class ConfigurationFunction extends BaseFunction {

	private static final long serialVersionUID = -6111536701353048922L;
	
	private final Converters converters;
	private final Map<String, Object> values;
	private final String name;
	private final Class<?> returnType;
	private final boolean allArgs;
	private final String defaultValue;

	ConfigurationFunction(
		final Converters converters,
		final Map<String, Object> values,
		final String name,
		final Class<?> returnType,
		final boolean allArgs,
		final String defaultValue
	) {
		this.converters = converters;
		this.values = values;
		this.name = name;
		this.returnType = returnType;
		this.allArgs = allArgs;
		this.defaultValue = defaultValue;
	}
	
	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		
		if (returnType.isArray()) {
			storeVector(args);
		} else {
			storeScalar(args);
		}
		
		
		return thisObj;
	}
	
	private void storeScalar(Object[] args) {
		values.put(name, getValue(args, returnType));
	}
	
	@SuppressWarnings("unchecked")
	private void storeVector(Object[] args) {
		if (!values.containsKey(name)) {
			values.put(name, new ArrayList<>());
		}
		((ArrayList<Object>)values.get(name)).add(getValue(args, returnType.getComponentType()));
	}

	private Object getValue(Object[] args, Class<?> type) {
		Object value = converters.convert(allArgs ? args : args[0], type);
		if (value == null && defaultValue != null) {
			value = converters.convert(defaultValue, returnType);
		}
		return value;
	}
	
}