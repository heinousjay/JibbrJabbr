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

import static org.mockito.BDDMockito.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import jj.configuration.Configuration;
import jj.engine.EngineAPI;
import jj.event.Publisher;
import jj.logging.EmergencyLogger;
import jj.resource.asset.Asset;
import jj.resource.asset.AssetResource;
import jj.resource.config.ConfigResource;
import jj.resource.css.CssResource;
import jj.resource.document.DocumentScriptEnvironment;
import jj.resource.document.HtmlResource;
import jj.resource.property.PropertiesResource;
import jj.resource.script.ModuleParent;
import jj.resource.script.ModuleScriptEnvironment;
import jj.resource.script.ScriptResource;
import jj.resource.sha1.Sha1Resource;
import jj.resource.spec.SpecResource;
import jj.resource.stat.ic.StaticResource;
import jj.script.RhinoContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;

/**
 * @author jason
 *
 */
public class ResourceInstanceCreatorTest extends RealResourceBase {
	
	public static ResourceInstanceCreator creator(final Configuration configuration, final Logger logger) {
		return new ResourceInstanceCreator(Guice.createInjector(new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(Configuration.class).toInstance(configuration);
				bind(Logger.class).annotatedWith(EmergencyLogger.class).toInstance(logger);
			}
		}), logger);
	}
	
	@Mock Publisher publisher;
	@Mock EngineAPI api;
	@Mock ResourceFinder resourceFinder;
	@Mock RhinoContext rhinoContext;
	
	private ResourceInstanceCreator makeCreator(final Configuration configuration, final Logger logger) {
		return new ResourceInstanceCreator(Guice.createInjector(new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(Configuration.class).toInstance(configuration);
				bind(Logger.class).annotatedWith(EmergencyLogger.class).toInstance(logger);
				bind(Publisher.class).toInstance(publisher);
				bind(EngineAPI.class).toInstance(api);
				bind(ResourceFinder.class).toInstance(resourceFinder);
				bind(RhinoContext.class).toInstance(rhinoContext);
			}
		}), logger);
	}
	
	ResourceInstanceCreator rimc;
	
	@Before
	public void before() {
		rimc = makeCreator(configuration, logger);
	}
	
	private <T extends Resource> T doCreate(Class<T> type, String baseName, Object...args) throws Exception {
		Path path = appPath.resolve(baseName);
		
		return doCreate(type, baseName, path, args);
	}
	
	private <T extends Resource> T doCreate(Class<T> type, String baseName, Path path, Object...args) throws Exception {
		ResourceCacheKey cacheKey = new ResourceCacheKey(type, path.toUri());
		
		return testResource(rimc.createResource(type, cacheKey, baseName, path, args));
	}
	
	@Test
	public void testAssetResource() throws Exception {
		doCreate(AssetResource.class, "jj.js", Asset.path);
	}
	
	@Test
	public void testConfigResource() throws Exception {
		given(rhinoContext.callFunction(any(Function.class), any(Scriptable.class), any(Scriptable.class), anyVararg()))
			.willReturn(Collections.EMPTY_MAP);
		doCreate(ConfigResource.class, ConfigResource.CONFIG_JS);
	}
	
	@Test
	public void testCssResource() throws Exception {
		doCreate(CssResource.class, "style.css", appPath.resolve("style.less"), true);
		doCreate(CssResource.class, "style.css", appPath.resolve("../jj/resource/test.css"));
	}
	
	@Test
	public void testHtmlResource() throws Exception {
		doCreate(HtmlResource.class, "index.html");
	}
	
	@Test
	public void testPropertiesResource() throws Exception {
		doCreate(PropertiesResource.class, "index.properties");
	}
	
	@Test
	public void testScriptResource() throws Exception {
		doCreate(ScriptResource.class, "helpers.js");
	}
	
	@Test
	public void testSha1Resource() throws Exception {
		doCreate(Sha1Resource.class, "not.real.test.sha1");
	}
	
	@Test
	public void testSpecResource() throws Exception {
		doCreate(SpecResource.class, "its_a_spec.js", appPath.resolveSibling("specs").resolve("its_a_spec.js"));
	}

	@Test
	public void testStaticResource() throws Exception {
		doCreate(StaticResource.class, "index.html");
	}
	
	@Test
	public void testDocumentScriptEnvironment() throws Exception {
		HtmlResource hr = mock(HtmlResource.class);
		given(resourceFinder.loadResource(HtmlResource.class, "index.html")).willReturn(hr);
		given(api.global()).willReturn(new NativeObject());
		
		doCreate(DocumentScriptEnvironment.class, "index", Paths.get("/"));
	}
	
	@Test
	public void testModuleScriptEnvironment() throws Exception {
		DocumentScriptEnvironment dse = mock(DocumentScriptEnvironment.class);
		ScriptResource sr = mock(ScriptResource.class);
		given(sr.script()).willReturn("");
		given(api.global()).willReturn(new NativeObject());
		given(resourceFinder.loadResource(ScriptResource.class, "index.js")).willReturn(sr);
		given(resourceFinder.findResource(dse)).willReturn(dse);
		given(rhinoContext.newObject(any(ScriptableObject.class))).willReturn(new NativeObject());
		
		doCreate(ModuleScriptEnvironment.class, "index", new ModuleParent(dse));
	}
}
