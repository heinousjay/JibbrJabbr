package jj.document.servable;


import static jj.AnswerWithSelf.ANSWER_WITH_SELF;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import jj.document.CurrentDocumentRequestProcessor;
import jj.document.DocumentScriptEnvironment;
import jj.document.servable.DocumentFilter;
import jj.document.servable.DocumentRequestProcessor;
import jj.execution.MockTaskRunner;
import jj.http.server.HttpServerRequest;
import jj.http.server.HttpServerResponse;
import jj.http.server.uri.URIMatch;
import jj.script.PendingKey;
import jj.script.DependsOnScriptEnvironmentInitialization;
import jj.script.ScriptTask;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Callable;

@RunWith(MockitoJUnitRunner.class)
public class DocumentRequestProcessorTest {
	
	Document document;
	String baseName;
	
	MockTaskRunner taskRunner;
	
	@Mock DependsOnScriptEnvironmentInitialization initializer;

	@Mock DocumentScriptEnvironment documentScriptEnvironment;
	
	@Mock Callable callable;
	
	CurrentDocumentRequestProcessor currentDocument;
	
	@Mock Channel channel;
	
	@Mock HttpServerRequest httpRequest;
	HttpServerResponse httpResponse;
	
	PendingKey pendingKey;
	
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

		taskRunner = new MockTaskRunner();
		
		given(documentScriptEnvironment.name()).willReturn(baseName);
		given(documentScriptEnvironment.document()).willReturn(document);
		given(documentScriptEnvironment.charset()).willReturn(UTF_8);
		given(documentScriptEnvironment.contentType()).willReturn("text/html; charset=UTF-8");
		
		given(httpRequest.uriMatch()).willReturn(new URIMatch("/"));
		
		// auto-stubbing the builder pattern
		httpResponse = mock(HttpServerResponse.class, ANSWER_WITH_SELF);
		
		pendingKey = new PendingKey();
		
		currentDocument = new CurrentDocumentRequestProcessor();
	}
	
	private DocumentRequestProcessor toTest(Set<DocumentFilter> filters) {
		return new DocumentRequestProcessor(
			taskRunner,
			initializer,
			currentDocument,
			documentScriptEnvironment,
			httpRequest,
			httpResponse,
			filters
		);
	}
	
	@Test
	public void testRespondsDirectlyWhenNoServerScript() throws Exception {
		
		//given
		DocumentRequestProcessor toTest = toTest(Collections.<DocumentFilter>emptySet());
		
		toTest.process();
		
		assertThat(taskRunner.tasks.size(), is(1));
		assertThat(taskRunner.tasks.get(0), is(instanceOf(ScriptTask.class)));
		taskRunner.runUntilIdle();
		

		verify(httpResponse).header(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
		verify(httpResponse).header(HttpHeaderNames.CACHE_CONTROL, HttpHeaderValues.NO_STORE);
		verify(httpResponse).header(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
		verify(httpResponse).content(bytes);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testWaitsForInitialization() throws Exception {
		
		DocumentRequestProcessor toTest = toTest(Collections.<DocumentFilter>emptySet());
		given(documentScriptEnvironment.hasServerScript()).willReturn(true);
		
		toTest.process();
		taskRunner.runUntilIdle();
		
		verify(initializer).executeOnInitialization(eq(documentScriptEnvironment), any(ScriptTask.class));
	}
	
	@Test
	public void testInvokesReadyFunctionWithInitializedScript() throws Exception {
		
		// given
		DocumentRequestProcessor toTest = toTest(Collections.<DocumentFilter>emptySet());
		given(documentScriptEnvironment.hasServerScript()).willReturn(true);
		given(documentScriptEnvironment.initialized()).willReturn(true);
		given(documentScriptEnvironment.getFunction(DocumentScriptEnvironment.READY_FUNCTION_KEY)).willReturn(callable);
		
		toTest.process();
		
		taskRunner.runUntilIdle();
		
		verify(documentScriptEnvironment).execute(callable);
		
		verify(httpResponse).header(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
		verify(httpResponse).header(HttpHeaderNames.CACHE_CONTROL, HttpHeaderValues.NO_STORE);
		verify(httpResponse).header(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
		verify(httpResponse).content(bytes);
		
		verifyZeroInteractions(initializer);
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
		toTest.respond();
		taskRunner.runUntilIdle();

		// then
		assertThat(filterCalls, is(6));
		// TODO can make this smarter later
	}
	
	@Test
	public void testWritesDocumentCorrectly() throws Exception {
		// given
		DocumentRequestProcessor toTest = toTest(Collections.<DocumentFilter>emptySet());
		
		// when
		toTest.respond();
		taskRunner.runUntilIdle();
		
		// then
		verify(httpResponse).header(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
		verify(httpResponse).header(HttpHeaderNames.CACHE_CONTROL, HttpHeaderValues.NO_STORE);
		verify(httpResponse).header(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
		verify(httpResponse).content(bytes);
	}

}
