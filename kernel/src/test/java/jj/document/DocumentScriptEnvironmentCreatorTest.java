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
package jj.document;

import static org.mockito.BDDMockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import jj.configuration.AppLocation;
import jj.document.DocumentScriptEnvironment;
import jj.document.DocumentScriptEnvironmentCreator;
import jj.resource.ResourceKey;
import jj.resource.ResourceInstanceCreator;

@RunWith(MockitoJUnitRunner.class)
public class DocumentScriptEnvironmentCreatorTest {

	private @Mock ResourceInstanceCreator creator;
	
	private DocumentScriptEnvironmentCreator toTest;
	
	@Before
	public void before() {
		toTest = new DocumentScriptEnvironmentCreator(null, creator);
	}
	
	@Test
	public void test() throws Exception {
		
		String name = "index";
		
		toTest.createScriptEnvironment(name);
		
		verify(creator).createResource(
			eq(DocumentScriptEnvironment.class),
			any(ResourceKey.class),
			eq(AppLocation.Virtual),
			eq(name)
		);
	}
}
