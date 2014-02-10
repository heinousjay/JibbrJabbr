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

import java.net.URL;

import jj.Closer;
import jj.engine.DollarFunction;
import jj.engine.EngineAPI;
import jj.resource.document.CurrentDocument;
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
		URL url = getClass().getResource(getClass().getSimpleName() + ".html");
		return Jsoup.parse(readPath(url));
	}

	//@Ignore
	@Test
	public void test() throws Exception {
		
		CurrentDocument document = new CurrentDocument();
		
		try (Closer closer = document.enterScope(document())) {
			EngineAPI host = makeHost(new DollarFunction(context, null, document, null));
			basicExecution(host);
		}
	}

}
