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
package jj.resource;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class ScriptResourceCreatorTest extends ResourceBase {

	@Test
	public void test() throws Exception {
		doTest("index", ScriptResourceType.Client);
		doTest("index", ScriptResourceType.Server);
		doTest("index", ScriptResourceType.Shared);
	}
	
	private void doTest(final String baseName, final ScriptResourceType type) throws Exception {
		ScriptResource sr = testFileResource(baseName, new ScriptResourceCreator(configuration), type);
		assertThat(sr.absoluteUri(), is(baseUri + sr.sha1() + "/" + baseName + type.suffix()));
	}

}
