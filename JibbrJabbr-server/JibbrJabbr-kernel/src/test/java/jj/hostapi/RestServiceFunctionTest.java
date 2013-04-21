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
package jj.hostapi;

import static org.mockito.BDDMockito.*;



import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class RestServiceFunctionTest extends AbstractHostApiTest {

	@Mock RestCallProvider restCallProvider;
	
	@Test
	public void test() throws Exception {
		
		// given
		RestServiceFunction restServiceFunction = new RestServiceFunction(restCallProvider);
		
		RhinoObjectCreator host = makeHost(restServiceFunction);
		
		// when
		basicExecution(host);
		
		verify(restCallProvider).createRestCall(any(RestCallOptions.class));
		
	}

}
