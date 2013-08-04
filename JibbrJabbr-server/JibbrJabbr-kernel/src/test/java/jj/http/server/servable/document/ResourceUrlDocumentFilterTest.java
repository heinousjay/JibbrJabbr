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
import jj.http.server.servable.document.DocumentRequestProcessor;
import jj.http.server.servable.document.ResourceUrlDocumentFilter;
import jj.resource.CssResource;
import jj.resource.ResourceFinder;
import jj.resource.StaticResource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceUrlDocumentFilterTest {

	@Mock ResourceFinder resourceFinder;
	@Mock DocumentRequestProcessor documentRequestProcessor;
	Document document;
	
	@Mock CssResource cssResource;
	@Mock StaticResource staticResource;
	@Mock StaticResource staticResource2;
	
	@InjectMocks ResourceUrlDocumentFilter filter;
	
	@Before
	public void before() {
		document = Jsoup.parse("<a href='style.css'><img src='style.gif'/></a><link href='thing-1.2.0.gif'/>");
		given(documentRequestProcessor.document()).willReturn(document);
	}
	
	@Test
	public void test() {
		
		String uri1 = "uri1";
		String uri2 = "uri2";
		
		given(resourceFinder.loadResource(CssResource.class, "style.css")).willReturn(cssResource);
		given(cssResource.uri()).willReturn(uri1);
		given(resourceFinder.loadResource(StaticResource.class, "style.gif")).willReturn(staticResource);
		given(staticResource.uri()).willReturn(uri2);
		given(resourceFinder.loadResource(StaticResource.class, "thing-1.2.0.gif")).willReturn(staticResource2);
		given(staticResource2.uri()).willReturn("gibberish");
		
		given(documentRequestProcessor.uri()).willReturn("/");
		filter.filter(documentRequestProcessor);
		
		assertThat(document.select("a").attr("href"), is(uri1));
		assertThat(document.select("img").attr("src"), is(uri2));
		assertThat(document.select("link").attr("href"), is("/thing-1.2.0.gif"));
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
