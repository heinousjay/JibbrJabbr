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
package jj.document.servable;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.*;
import jj.css.StylesheetResource;
import jj.document.servable.DocumentRequestProcessor;
import jj.document.servable.ResourceUrlDocumentFilter;
import jj.http.server.resource.StaticResource;
import jj.http.server.uri.URIMatch;
import jj.resource.ServableLoader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceUrlDocumentFilterTest {
	
	private @Mock DocumentRequestProcessor documentRequestProcessor;
	private Document document;
	
	private @Mock StylesheetResource cssResource;
	private String cssResourcePath = "css/path.css";
	private @Mock StaticResource staticResource1;
	private String baseStaticPath = "base/static.path";
	private @Mock StaticResource staticResource2;
	private String assetStaticPath = "asset/static-1.2.path";
	
	@Mock ServableLoader servableLoader;
	
	private ResourceUrlDocumentFilter filter;
	
	@Before
	public void before() {
		
		document = Jsoup.parse(
			"<a href='" + cssResourcePath + "'>" +
				"<img src='" + baseStaticPath + "'/>" +
			"</a><link href='" + assetStaticPath + "'/>");
		given(documentRequestProcessor.document()).willReturn(document);
		
		filter = new ResourceUrlDocumentFilter(servableLoader);
	}
	
	@Test
	public void test() {
		
		willReturn(cssResource).given(servableLoader).loadResource(new URIMatch("/" + cssResourcePath));
		willReturn(staticResource1).given(servableLoader).loadResource(new URIMatch("/" + baseStaticPath));
		willReturn(staticResource2).given(servableLoader).loadResource(new URIMatch("/" + assetStaticPath));
		
		given(cssResource.serverPath()).willReturn("/substitutesha/" + cssResourcePath);
		given(staticResource1.serverPath()).willReturn("/substitutesha/" + baseStaticPath);
		given(staticResource2.serverPath()).willReturn("/substitutesha/" + assetStaticPath);
		
		given(documentRequestProcessor.uri()).willReturn("/");
		
		// when
		filter.filter(documentRequestProcessor);
		
		// then
		assertThat(document.select("a").attr("href"), is("/substitutesha/" + cssResourcePath));
		assertThat(document.select("img").attr("src"), is("/substitutesha/" + baseStaticPath));
		assertThat(document.select("link").attr("href"), is("/" + assetStaticPath));
	}
	
	@Test
	public void test2() {
		
		// given
		document.select("img").attr("src", "../style.gif");
		given(documentRequestProcessor.uri()).willReturn("/files/");
		
		// when
		filter.filter(documentRequestProcessor);
		
		// then
		assertThat(document.select("a").attr("href"), is("/files/" + cssResourcePath));
		assertThat(document.select("img").attr("src"), is("/style.gif"));
		assertThat(document.select("link").attr("href"), is("/files/" + assetStaticPath));
	}
	
}
