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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jj.configuration.Configuration;

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
public class ConfigResourceCreatorTest {
	
	Path basePath;
	@Mock Configuration configuration;
	
	ConfigResourceCreator toTest;
	
	@Before
	public void before() throws Exception {
		basePath = Paths.get(ResourceBase.class.getResource("/config.js").toURI()).getParent();
		given(configuration.basePath()).willReturn(basePath);
		
		toTest = new ConfigResourceCreator(configuration);
	}

	@Test
	public void testCreate() throws Exception {
		
		ConfigResource resource = toTest.create(ConfigResource.CONFIG_JS);
		
		assertThat(resource.script().getBytes(UTF_8), is(Files.readAllBytes(basePath.resolve(ConfigResource.CONFIG_JS))));
	}

}
