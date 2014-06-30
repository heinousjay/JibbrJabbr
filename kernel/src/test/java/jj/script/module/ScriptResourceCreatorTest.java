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
package jj.script.module;

import static jj.configuration.resolution.AppLocation.Base;
import static org.mockito.BDDMockito.*;
import jj.configuration.resolution.MockApplication;
import jj.resource.ResourceInstanceCreator;
import jj.resource.ResourceKey;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ScriptResourceCreatorTest {
	
	MockApplication app;
	ScriptResourceCreator src;
	@Mock ResourceInstanceCreator instanceCreator;

	@Test
	public void test() throws Exception {
		String name = "name";
		app = new MockApplication();
		src = new ScriptResourceCreator(app, instanceCreator);
		
		src.create(Base, name);
		
		verify(instanceCreator).createResource(
			eq(ScriptResource.class),
			isA(ResourceKey.class),
			eq(Base),
			eq(name)
		);
	}

}
