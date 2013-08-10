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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jj.CoreConfiguration;
import jj.JJ;
import jj.configuration.Configuration;
import jj.execution.ExecutionTrace;
import jj.resource.LessProcessor;

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
public class LessProcessorTest {

	LessProcessor underTest;
	Path appPath;
	String testCss;
	@Mock CoreConfiguration coreConfiguration;
	@Mock Configuration configuration;
	@Mock ExecutionTrace trace;
	
	@Before
	public void before() throws IOException {
		appPath = Paths.get(JJ.uri(LessProcessorTest.class)).getParent();
		testCss = new String(Files.readAllBytes(appPath.resolve("test.css")), UTF_8);
		given(configuration.get(CoreConfiguration.class)).willReturn(coreConfiguration);
		given(coreConfiguration.appPath()).willReturn(appPath);
		underTest = new LessProcessor(configuration, trace);
	}
	
	@Test
	public void testBasicProcessing() throws IOException {
		
		String output = underTest.process("test.less");
		// System.out.println(output);
		// we don't so much care that less does what less does
		// as we care that it worked.
		assertThat(output, is(testCss));
		
		verify(trace).startLessProcessing("test.less");
		verify(trace).loadLessResource("test.less");
		verify(trace).loadLessResource("test2.less");
		verify(trace).finishLessProcessing("test.less");
	}

}
