package jj.http.server.servable.document;


import static jj.AnswerWithSelf.ANSWER_WITH_SELF;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import jj.execution.MockJJExecutor;
import jj.execution.ScriptTask;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.http.server.servable.document.DocumentFilter;
import jj.resource.MimeTypes;
import jj.resource.document.DocumentScriptEnvironment;
import jj.script.ScriptRunner;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DocumentRequestProcessorTest {
	
	Document document;
	String baseName;
	
	@Mock ScriptRunner scriptRunner;
	
	MockJJExecutor executors;

	@Mock DocumentScriptEnvironment documentScriptEnvironment;
	
	@Mock Channel channel;
	@Mock Logger access;
	
	@Mock HttpRequest httpRequest;
	HttpResponse httpResponse;
	
	int filterCalls;
	
	class TestDocumentFilter implements DocumentFilter {

		private boolean io;
		private int sequence;
		
		TestDocumentFilter(boolean io, int sequence) {
			this.io = io;
			this.sequence = sequence;
		}
		
		@Override
		public boolean needsIO(DocumentRequestProcessor documentRequestProcessor) {
			return io;
		}

		@Override
		public void filter(DocumentRequestProcessor documentRequestProcessor) {
			assertThat(++filterCalls, is(sequence));
		}
	}
	
	private static final String HTML = "<html><head><title>what</title></head><body></body></html>";
	
	byte[] bytes = HTML.getBytes(UTF_8);
	
	@Before
	public void before() {
		
		filterCalls = 0;
		
		document = Jsoup.parse(HTML);
		document.outputSettings().prettyPrint(false);
		baseName = "baseName";

		executors = new MockJJExecutor();
		
		given(documentScriptEnvironment.baseName()).willReturn(baseName);
		given(documentScriptEnvironment.document()).willReturn(document);
		
		given(httpRequest.uri()).willReturn("/");
		
		// auto-stubbing the builder pattern
		httpResponse = mock(HttpResponse.class, ANSWER_WITH_SELF);
	}
	
	private DocumentRequestProcessor toTest(Set<DocumentFilter> filters) {
		return new DocumentRequestProcessor(executors, scriptRunner, documentScriptEnvironment, httpRequest, httpResponse, filters);
	}
	
	@Test
	public void testRespondsDirectlyWhenNoServerScript() throws Exception {
		
		//given
		DocumentRequestProcessor toTest = toTest(Collections.<DocumentFilter>emptySet());
		
		toTest.process();
		
		assertThat(executors.tasks.size(), is(1));
		assertThat(executors.tasks.get(0), is(instanceOf(ScriptTask.class)));
		executors.isScriptThread = true;
		executors.runUntilIdle();
		

		verify(httpResponse).header(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
		verify(httpResponse).header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE);
		verify(httpResponse).header(HttpHeaders.Names.CONTENT_TYPE, MimeTypes.get(".html"));
		verify(httpResponse).content(bytes);
	}
	
	@Test
	public void testInvokesScriptRunnerWhenScriptIsPresent() throws Exception {
		
		// given
		DocumentRequestProcessor toTest = toTest(Collections.<DocumentFilter>emptySet());
		given(documentScriptEnvironment.hasServerScript()).willReturn(true);
		
		toTest.process();
		
		verify(scriptRunner).submit(toTest);
	}

	@Test
	public void testIOFilterBehavior() throws Exception {

		// given
		Set<DocumentFilter> filters = new LinkedHashSet<>();
		filters.add(new TestDocumentFilter(true, 4));
		filters.add(new TestDocumentFilter(true, 5));
		filters.add(new TestDocumentFilter(false, 1));
		filters.add(new TestDocumentFilter(false, 2));
		filters.add(new TestDocumentFilter(true, 6));
		filters.add(new TestDocumentFilter(false, 3));
		
		DocumentRequestProcessor toTest = toTest(filters);
		
		// when
		executors.isScriptThread = true;
		toTest.respond();
		executors.runUntilIdle();

		// then
		assertThat(filterCalls, is(6));
		// TODO can make this smarter later
	}
	
	@Test
	public void testWritesDocumentCorrectly() throws Exception {
		// given
		DocumentRequestProcessor toTest = toTest(Collections.<DocumentFilter>emptySet());
				
		
		executors.isScriptThread = true;
		
		// when
		toTest.respond();
		executors.runUntilIdle();
		
		// then
		verify(httpResponse).header(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
		verify(httpResponse).header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE);
		verify(httpResponse).header(HttpHeaders.Names.CONTENT_TYPE, MimeTypes.get(".html"));
		verify(httpResponse).content(bytes);
	}

}
