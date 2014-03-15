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

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.util.Map;

import jj.logging.EmergencyLogger;

import org.junit.Test;
import org.slf4j.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;

/**
 * @author jason
 *
 */
public class CoreModuleTest {

	@Test
	public void testRunningBuild() {
		
		// this should be enough to test that the core module builds
		Injector injector = Guice.createInjector(Stage.PRODUCTION, new CoreModule(new String[0], new BootstrapClassPath()));
		
		// force it to try to instantiate everything
		injector.getInstance(JJServerLifecycle.class);
		
		// and for now this lives here - a vital inventory! we must have the emergency logger configured or we
		// will lose errors and have no idea what is broken
		Map<Class<? extends Annotation>, Logger> loggers = 
			injector.getInstance(Key.get(new TypeLiteral<Map<Class<? extends Annotation>, Logger>>() {}));
		
		assertTrue(loggers.containsKey(EmergencyLogger.class));
	}
	
	
	@Test
	public void testTestBuild() {
// almost not a thing! wheee!
		Guice.createInjector(Stage.PRODUCTION, new CoreModule(new String[0])).getInstance(JJServerLifecycle.class);
	}
}
