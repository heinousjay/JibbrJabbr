package jj.http.server.servable.document;


import static jj.AnswerWithSelf.ANSWER_WITH_SELF;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import jj.execution.MockJJExecutor;
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

	@Mock DocumentScriptEnvironment dse;
	
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
	
	@Before
	public void before() {
		
		filterCalls = 0;
		
		document = Jsoup.parse(HTML);
		document.outputSettings().prettyPrint(false);
		baseName = "baseName";

		executors = new MockJJExecutor();
		
		when(dse.baseName()).thenReturn(baseName);
		when(dse.document()).thenReturn(document);
		
		when(httpRequest.uri()).thenReturn("/");
		
		// auto-stubbing the builder pattern
		httpResponse = mock(HttpResponse.class, ANSWER_WITH_SELF);
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
		
		DocumentRequestProcessor toTest = 
			new DocumentRequestProcessor(executors, scriptRunner, dse, httpRequest, httpResponse, filters);
		
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
		DocumentRequestProcessor toTest = 
				new DocumentRequestProcessor(executors, scriptRunner, dse, httpRequest, httpResponse, Collections.<DocumentFilter>emptySet());
		
		executors.isScriptThread = true;
		
		byte[] bytes = HTML.getBytes(UTF_8);
		
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
