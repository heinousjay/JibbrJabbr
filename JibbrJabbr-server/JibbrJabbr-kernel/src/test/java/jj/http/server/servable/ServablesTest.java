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
package jj.http.server.servable;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jj.resource.AssetResource;
import jj.resource.CssResource;
import jj.resource.Resource;
import jj.resource.StaticResource;
import jj.uri.URIMatch;

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
public class ServablesTest {
	
	URIMatch cssMatch = new URIMatch("/something.css");
	URIMatch otherMatch = new URIMatch("/other.thing");
	
	@Mock Servable<AssetResource> assetServable;
	@Mock Servable<CssResource> cssServable;
	@Mock Servable<StaticResource> staticServable;
	
	Servables servables;
	
	@Before
	public void before() {
		
		given(assetServable.type()).willReturn(AssetResource.class);
		given(assetServable.isMatchingRequest(otherMatch)).willReturn(true);
		given(cssServable.type()).willReturn(CssResource.class);
		given(cssServable.isMatchingRequest(cssMatch)).willReturn(true);
		given(staticServable.type()).willReturn(StaticResource.class);
		given(staticServable.isMatchingRequest(otherMatch)).willReturn(true);
		given(staticServable.isMatchingRequest(cssMatch)).willReturn(true);
		
		Set<Servable<? extends Resource>>  servablesSet = new HashSet<>();
		servablesSet.add(assetServable);
		servablesSet.add(cssServable);
		servablesSet.add(staticServable);
		
		servables = new Servables(servablesSet);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMatchingRequest() {
		
		List<Servable<? extends Resource>> result;
		
		result = servables.findMatchingServables(cssMatch);
		assertThat(result.size(), is(2));
		assertThat(result, containsInAnyOrder((Servable<? extends Resource>)cssServable, staticServable));
		
		result = servables.findMatchingServables(otherMatch);
		assertThat(result.size(), is(2));
		assertThat(result, containsInAnyOrder((Servable<? extends Resource>)assetServable, staticServable));
	}
	
	@Test
	public void testTypeMatch() {
		
		assertThat(servables.findMatchingServable(AssetResource.class), is(assetServable));
		assertThat(servables.findMatchingServable(CssResource.class), is(cssServable));
		assertThat(servables.findMatchingServable(StaticResource.class), is(staticServable));
	}

}