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
package jj.servable;

import static org.hamcrest.Matchers.*;
import static org.jboss.netty.util.CharsetUtil.UTF_8;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jj.Configuration;
import jj.JJ;

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
	Path basePath;
	String testCss;
	@Mock Configuration configuration;
	
	@Before
	public void before() throws IOException {
		basePath = Paths.get(JJ.uri(LessProcessorTest.class)).getParent();
		testCss = new String(Files.readAllBytes(basePath.resolve("test.css")), UTF_8);
		given(configuration.basePath()).willReturn(basePath);
		underTest = new LessProcessor(configuration);
	}
	
	@Test
	public void testBasicProcessing() throws IOException {
		
		String output = underTest.process("test.less");
		
		// we don't so much care that less does what less does
		// as we care that it worked.
		assertThat(output, is(testCss));
	}

}
