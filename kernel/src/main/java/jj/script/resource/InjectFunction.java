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
package jj.script.resource;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.google.inject.Injector;

/**
 * <p>
 * A rhino host object exposed to API modules to allow them to
 * get instances from the core injector.  For now it'll be wrapped up behind
 * a function call, it returns whatever getInstance returns, and that's about
 * it.
 * 
 * <p>
 * Basic concept is that a ModuleScriptEnvironment that is loaded from the
 * AppLocation.Assets location will have this function defined in its scope.
 * this allows internal scripts to load whatever they need from the surrounding
 * environment - getting rid of the "host object" necessity and allowing
 * the API to be written as script where appropriate and with no need to wrap up
 * the pieces behind the scenes where java makes more sense.
 * 
 * <p>
 * This class is not actually serializable! this may need to be taken into
 * account at some point - basically this object cannot be shared since it
 * requires the injector to work.
 * 
 * @author jason
 *
 */
@Singleton
class InjectFunction extends BaseFunction {
	
	static final String NAME = "inject";

	private static final long serialVersionUID = 1L;
	
	private transient final Injector injector;
	
	@Inject
	InjectFunction(final Injector injector) {
		this.injector = injector;
	}
	
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new AssertionError("don't new the " + NAME + " function");
	}
	
	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		assert args.length == 1 && args[0] instanceof CharSequence : NAME + " requires one string argument";
		try {
			Class<?> desiredClass = Class.forName(String.valueOf(args[0]));
			return injector.getInstance(desiredClass);
			
		} catch (Exception e) {
			throw new AssertionError(NAME + " failed", e);
		}
	}

}
