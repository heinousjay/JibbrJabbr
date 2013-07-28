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

import static org.mockito.BDDMockito.*;
import jj.http.server.servable.document.DocumentRequestProcessor;
import jj.http.server.servable.document.ResourceUrlDocumentFilter;
import jj.resource.ResourceFinder;

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

	@Mock ResourceFinder resourceFinder;
	@Mock DocumentRequestProcessor documentRequestProcessor;
	Document document;
	
	@Before
	public void before() {
		document = Jsoup.parse("<a href='style.css'>style</a>");
		given(documentRequestProcessor.document()).willReturn(document);
		
	}
	
	@Test
	public void test() {
		ResourceUrlDocumentFilter underTest = new ResourceUrlDocumentFilter(resourceFinder);
		given(documentRequestProcessor.uri()).willReturn("/");
		underTest.filter(documentRequestProcessor);
		given(documentRequestProcessor.uri()).willReturn("/files");
		underTest.filter(documentRequestProcessor);
	}
	
}