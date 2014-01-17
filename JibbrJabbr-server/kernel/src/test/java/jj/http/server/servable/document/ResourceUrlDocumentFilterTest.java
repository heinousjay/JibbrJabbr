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
package jj.http.server.servable.document;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.*;
import jj.http.server.MockServablesRule;
import jj.http.server.servable.document.DocumentRequestProcessor;
import jj.http.server.servable.document.ResourceUrlDocumentFilter;
import jj.resource.asset.AssetResource;
import jj.resource.css.CssResource;
import jj.resource.stat.ic.StaticResource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Rule;
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
	
	@Rule
	public MockServablesRule m = new MockServablesRule();

	@Mock DocumentRequestProcessor documentRequestProcessor;
	Document document;
	
	@Mock CssResource cssResource;
	@Mock StaticResource staticResource;
	@Mock AssetResource assetResource;
	
	ResourceUrlDocumentFilter filter;
	
	@Before
	public void before() {
		document = Jsoup.parse(
			"<a href='" + m.cssUri.baseName + "'>" +
				"<img src='" + m.staticUri.baseName + "'/>" +
			"</a><link href='" + m.assetUri.baseName + "'/>");
		given(documentRequestProcessor.document()).willReturn(document);
		
		filter = new ResourceUrlDocumentFilter(m.servables);
	}
	
	@Test
	public void test() {
		
		given(m.servables.loadResource(m.cssUri)).willReturn(cssResource);
		given(cssResource.uri()).willReturn("/substitutesha" + m.cssUri.uri);
		
		given(m.servables.loadResource(m.assetUri)).willReturn(assetResource);
		given(assetResource.uri()).willReturn("/substitutesha" + m.assetUri.uri);
		
		given(m.servables.loadResource(m.staticUri)).willReturn(staticResource);
		given(staticResource.uri()).willReturn("/substitutesha" + m.staticUri.uri);
		
		given(documentRequestProcessor.uri()).willReturn("/");
		filter.filter(documentRequestProcessor);
		
		assertThat(document.select("a").attr("href"), is("/substitutesha" + m.cssUri.uri));
		assertThat(document.select("img").attr("src"), is("/substitutesha" + m.staticUri.uri));
		assertThat(document.select("link").attr("href"), is(m.assetUri.uri));
	}
	
	@Test
	public void test2() {
		
		document.select("img").attr("src", "../style.gif");
		given(documentRequestProcessor.uri()).willReturn("/files/");
		filter.filter(documentRequestProcessor);
		
		assertThat(document.select("a").attr("href"), is("/files/style.css"));
		assertThat(document.select("img").attr("src"), is("/style.gif"));
		assertThat(document.select("link").attr("href"), is("/files/thing-1.2.0.gif"));
	}
	
}
