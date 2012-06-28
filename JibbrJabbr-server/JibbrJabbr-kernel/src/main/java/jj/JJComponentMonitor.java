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

import java.lang.reflect.Constructor;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.monitors.NullComponentMonitor;

/**
 * @author jason
 *
 */
public class JJComponentMonitor extends NullComponentMonitor {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5754941079119566064L;

	
	/* (non-Javadoc)
	 * @see org.picocontainer.monitors.NullComponentMonitor#instantiated(org.picocontainer.PicoContainer, org.picocontainer.ComponentAdapter, java.lang.reflect.Constructor, java.lang.Object, java.lang.Object[], long)
	 */
	@Override
	public <T> void instantiated(PicoContainer container,
			ComponentAdapter<T> componentAdapter, Constructor<T> constructor,
			Object instantiated, Object[] injected, long duration) {
		
		container.getComponent(EventMediationService.class).register(instantiated);
	}
	
}
