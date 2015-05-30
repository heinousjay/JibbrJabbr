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

import static io.netty.handler.codec.http.HttpMethod.GET;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.*;
import jj.css.StylesheetResource;
import jj.document.servable.DocumentRequestProcessor;
import jj.document.servable.ResourceUrlDocumentFilter;
import jj.http.server.RouteProcessor;
import jj.http.server.ServableResources;
import jj.http.server.resource.StaticResource;
import jj.http.server.uri.Route;
import jj.http.server.uri.RouteMatch;
import jj.http.server.uri.Router;
import jj.http.server.uri.URIMatch;
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
	
	@Mock ServableResources servables;
	@Mock Router router;
	@Mock Route route;
	
	@Mock RouteMatch routeMatch1;
	@Mock RouteMatch routeMatch2;
	@Mock RouteMatch routeMatch3;

	@Mock RouteProcessor routeProcessor;
	
	@Mock DocumentRequestProcessor documentRequestProcessor;
	Document document;
	
	@Mock StylesheetResource cssResource;
	String cssResourcePath = "css/path.css";
	@Mock StaticResource staticResource1;
	String baseStaticPath = "base/static.path";
	@Mock StaticResource staticResource2;
	String assetStaticPath = "asset/static-1.2.path";
	
	ResourceUrlDocumentFilter filter;
	
	@Before
	public void before() {
		
		document = Jsoup.parse(
			"<a href='" + cssResourcePath + "'>" +
				"<img src='" + baseStaticPath + "'/>" +
			"</a><link href='" + assetStaticPath + "'/>");
		given(documentRequestProcessor.document()).willReturn(document);
		
		filter = new ResourceUrlDocumentFilter(servables, router);
	}
	
	@Test
	public void test() {
		
		// given
		given(router.routeRequest(GET, new URIMatch("/" + cssResourcePath))).willReturn(routeMatch1);
		given(routeMatch1.matched()).willReturn(true);
		given(routeMatch1.resourceName()).willReturn("stylesheet");
		given(routeMatch1.route()).willReturn(route);
		given(router.routeRequest(GET, new URIMatch("/" + baseStaticPath))).willReturn(routeMatch2);
		given(routeMatch2.matched()).willReturn(true);
		given(routeMatch2.resourceName()).willReturn("static");
		given(routeMatch2.route()).willReturn(route);
		given(router.routeRequest(GET, new URIMatch("/" + assetStaticPath))).willReturn(routeMatch3);
		given(routeMatch3.matched()).willReturn(true);
		given(routeMatch3.resourceName()).willReturn("static");
		given(routeMatch3.route()).willReturn(route);
		
		willReturn(StylesheetResource.class).given(servables).classFor("stylesheet");
		willReturn(StaticResource.class).given(servables).classFor("static");
		
		given(servables.routeProcessor("stylesheet")).willReturn(routeProcessor);
		given(servables.routeProcessor("static")).willReturn(routeProcessor);
		
		given(routeProcessor.loadResource(StylesheetResource.class, new URIMatch("/" + cssResourcePath), route)).willReturn(cssResource);
		given(routeProcessor.loadResource(StaticResource.class, new URIMatch("/" + baseStaticPath), route)).willReturn(staticResource1);
		given(routeProcessor.loadResource(StaticResource.class, new URIMatch("/" + assetStaticPath), route)).willReturn(staticResource2);
		
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
		given(router.routeRequest(eq(GET), any(URIMatch.class))).willReturn(routeMatch1);
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
