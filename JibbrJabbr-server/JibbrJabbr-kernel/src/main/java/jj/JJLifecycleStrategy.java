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
package jj;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jj.api.Dispose;
import jj.api.Shutdown;
import jj.api.Startup;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.PicoLifecycleException;

/**
 * <p>
 * Simple mapping from picocontainer lifecycle to JOJ lifecycle.
 * </p>
 * 
 * <p>
 * See {@link Startup}, {@link Shutdown}, and {@link Dispose}.
 * </p>
 * 
 * @author jason
 *
 */
class JJLifecycleStrategy implements LifecycleStrategy {

	@Override
	public void start(Object component) {
		invoke(component, Startup.class);
	}

	@Override
	public void stop(Object component) {
		invoke(component, Shutdown.class);
	}

	@Override
	public void dispose(Object component) {
		invoke(component, Dispose.class);
	}
	
	private void invoke(Object component, Class<? extends Annotation> annotation) {
		for (Method method : component.getClass().getMethods()) {
			if (method.isAnnotationPresent(annotation)) {
				try {
					method.invoke(component);
				} catch (InvocationTargetException ite) {
					throw new PicoLifecycleException(method, component, ite.getCause());
				} catch (Exception e) {
					throw new PicoLifecycleException(method, component, e);
				}
			}
		}
	}

	@Override
	public boolean hasLifecycle(Class<?> type) {
		boolean result = false;
		for (Method method : type.getMethods()) {
			if (method.isAnnotationPresent(Startup.class) ||
				method.isAnnotationPresent(Shutdown.class) ||
				method.isAnnotationPresent(Dispose.class)) {
				result = true;
				break;
			}
		}
		return result;
	}

	@Override
	public boolean isLazy(ComponentAdapter<?> adapter) {
		return false; // we don't do lazy
	}

}
