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
package jj.http.server;

import static org.mockito.BDDMockito.*;

import java.util.Arrays;

import jj.http.server.servable.Servable;
import jj.http.server.servable.Servables;
import jj.resource.AssetResource;
import jj.resource.CssResource;
import jj.resource.Resource;
import jj.resource.StaticResource;
import jj.uri.URIMatch;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.Mock;

/**
 * @author jason
 *
 */
public class MockServablesRule implements TestRule {
	
	public final URIMatch staticUri = new URIMatch("/style.gif");
	public final URIMatch assetUri = new URIMatch("/thing-1.2.0.gif");
	public final URIMatch cssUri = new URIMatch("/style.css");
	public final URIMatch uri4 = new URIMatch("/4");
	public final URIMatch uri5 = new URIMatch("/5");
	
	@Mock public Servable<StaticResource> staticServable;
	@Mock public Servable<AssetResource> assetServable;
	@Mock public Servable<CssResource> cssServable;
	
	@Mock public Servables servables;
	
	@SuppressWarnings("unchecked")
	private void init() {
		
		staticServable = mock(Servable.class);
		assetServable = mock(Servable.class);
		cssServable = mock(Servable.class);
		servables = mock(Servables.class);
	}

	@Override
	public Statement apply(final Statement base, final Description description) {
		return new Statement() {
			
			@Override
			public void evaluate() throws Throwable {
				
				init();
				
				given(servables.findMatchingServables(staticUri))
					.willReturn(Arrays.<Servable<? extends Resource>>asList(staticServable));
				given(servables.findMatchingServables(assetUri))
					.willReturn(Arrays.<Servable<? extends Resource>>asList(assetServable));
				given(servables.findMatchingServables(cssUri))
					.willReturn(Arrays.<Servable<? extends Resource>>asList(cssServable));
				given(servables.findMatchingServables(uri4))
					.willReturn(Arrays.<Servable<? extends Resource>>asList(assetServable, cssServable));
				given(servables.findMatchingServables(uri5))
					.willReturn(Arrays.<Servable<? extends Resource>>asList(cssServable));
				
				base.evaluate();
			}
		};
	}

}
