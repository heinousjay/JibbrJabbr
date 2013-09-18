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

import java.nio.file.Path;

import jj.configuration.Configuration;
import jj.logging.EmergencyLogger;
import jj.resource.asset.Asset;
import jj.resource.asset.AssetResource;
import jj.resource.config.ConfigResource;
import jj.resource.css.CssResource;
import jj.resource.html.HtmlResource;
import jj.resource.property.PropertiesResource;
import jj.resource.script.ScriptResource;
import jj.resource.sha1.Sha1Resource;
import jj.resource.spec.SpecResource;
import jj.resource.stat.ic.StaticResource;

import org.junit.Before;
import org.junit.Test;
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
	
	ResourceInstanceCreator rimc;
	
	@Before
	public void before() {
		
		rimc = creator(configuration, logger);
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
}
