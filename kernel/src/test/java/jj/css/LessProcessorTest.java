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
package jj.css;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jj.configuration.Application;
import jj.css.FinishedLessProcessing;
import jj.css.LessProcessor;
import jj.css.LoadLessResource;
import jj.css.StartingLessProcessing;
import jj.event.MockPublisher;
import jj.script.RealRhinoContextProvider;

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
	@Mock Application app;
	MockPublisher publisher;
	
	@Before
	public void before() throws Exception {
		appPath = Paths.get(LessProcessorTest.class.getResource("/jj/resource/test.css").toURI()).getParent();
		testCss = new String(Files.readAllBytes(appPath.resolve("test.css")), UTF_8);
		given(app.path()).willReturn(appPath);
		publisher = new MockPublisher();
		underTest = new LessProcessor(app, new RealRhinoContextProvider(), publisher);
	}
	
	@Test
	public void testBasicProcessing() throws IOException {
		
		String output = underTest.process("test.less");
		// System.out.println(output);
		// we don't so much care that less does what less does
		// as we care that it worked.
		assertThat(output, is(testCss));
		
		Class<?>[] expected = new Class<?>[] {
			StartingLessProcessing.class,
			LoadLessResource.class,
			LoadLessResource.class,
			FinishedLessProcessing.class
		};
		
		assertThat(publisher.events.size(), is(expected.length));
		
		for (Class<?> clazz : expected) {
			assertThat(publisher.events.remove(0), is(instanceOf(clazz)));
		}
	}

}
