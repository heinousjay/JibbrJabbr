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

import static org.mockito.BDDMockito.given;

import java.nio.file.Path;
import java.nio.file.Paths;

import jj.JJ;
import jj.engine.DollarFunction;
import jj.engine.EngineAPI;
import jj.script.CurrentScriptContext;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author jason
 *
 */
public class DollarFunctionTest extends AbstractEngineApiTest {

	@Mock CurrentScriptContext context;
	
	private Document document() throws Exception {
		Path me = Paths.get(JJ.uri(getClass()));
		return Jsoup.parse(readPath(me.resolveSibling(DollarFunctionTest.class.getSimpleName() + ".html")));
	}
	
	//@Ignore
	@Test
	public void test() throws Exception {
		
		given(context.document()).willReturn(document());
		
		EngineAPI host = makeHost(new DollarFunction(context));
		basicExecution(host);
	}

}
