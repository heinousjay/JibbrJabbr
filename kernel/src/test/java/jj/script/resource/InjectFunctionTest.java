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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import jj.script.CurrentScriptEnvironment;
import jj.script.ScriptEnvironment;
import jj.script.resource.InjectFunction;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.Injector;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class InjectFunctionTest {
	
	public static final String NAME = InjectFunction.NAME;

	@Mock Injector injector;
	
	@InjectMocks InjectFunction ibf;
	
	@Mock ScriptEnvironment se;
	
	@Mock CurrentScriptEnvironment cse;
	
	@Test
	public void test() {
		
		given(injector.getInstance(ScriptEnvironment.class)).willReturn(se);
		given(injector.getInstance(CurrentScriptEnvironment.class)).willReturn(cse);
		
		Object result = ibf.call(null, null, null, new Object[] {ScriptEnvironment.class.getName()});
		
		assertThat(result, is((Object)se));
		
		result = ibf.call(null, null, null, new Object[] {CurrentScriptEnvironment.class.getName()});
		
		assertThat(result, is((Object)cse));
	}

}
