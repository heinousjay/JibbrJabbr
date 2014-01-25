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
package jj.engine;

import static org.mockito.BDDMockito.*;
import jj.engine.EngineAPI;
import jj.engine.RestCallOptions;
import jj.engine.RestCallProvider;
import jj.engine.RestServiceFunction;



import org.junit.Test;
import org.mockito.Mock;

/**
 * @author jason
 *
 */
public class RestServiceFunctionTest extends AbstractEngineApiTest {

	@Mock RestCallProvider restCallProvider;
	
	@Test
	public void test() throws Exception {
		
		// given
		RestServiceFunction restServiceFunction = new RestServiceFunction(restCallProvider);
		
		EngineAPI host = makeHost(restServiceFunction);
		
		// when
		basicExecution(host);
		
		// then
		verify(restCallProvider).createRestCall(any(RestCallOptions.class));
		
	}

}