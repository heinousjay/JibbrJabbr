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
import jj.execution.TaskHelper;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.http.server.servable.document.DocumentFilter;
import jj.resource.MimeTypes;
import jj.resource.document.CurrentDocumentRequestProcessor;
import jj.resource.document.DocumentScriptEnvironment;
import jj.script.ContinuationCoordinator;
import jj.script.ContinuationPendingKey;
import jj.script.DependsOnScriptEnvironmentInitialization;
import jj.script.ScriptEnvironment;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Callable;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DocumentRequestProcessorTest {
	
	Document document;
	String baseName;
	
	MockJJExecutor executor;
	
	@Mock DependsOnScriptEnvironmentInitialization initializer;
	
	@Mock ContinuationCoordinator continuationCoordinator;

	@Mock DocumentScriptEnvironment documentScriptEnvironment;
	
	@Mock Callable callable;
	
	CurrentDocumentRequestProcessor currentDocument;
	
	@Mock Channel channel;
	@Mock Logger access;
	
	@Mock HttpRequest httpRequest;
	HttpResponse httpResponse;
	
	ContinuationPendingKey pendingKey;
	
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

		executor = new MockJJExecutor();
		
		given(documentScriptEnvironment.baseName()).willReturn(baseName);
		given(documentScriptEnvironment.document()).willReturn(document);
		
		given(httpRequest.uri()).willReturn("/");
		
		// auto-stubbing the builder pattern
		httpResponse = mock(HttpResponse.class, ANSWER_WITH_SELF);
		
		pendingKey = new ContinuationPendingKey();
		
		currentDocument = new CurrentDocumentRequestProcessor();
	}
	
	private DocumentRequestProcessor toTest(Set<DocumentFilter> filters) {
		return new DocumentRequestProcessor(
			executor,
			initializer,
			continuationCoordinator,
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
		
		assertThat(executor.tasks.size(), is(1));
		assertThat(executor.tasks.get(0), is(instanceOf(ScriptTask.class)));
		executor.isScriptThread = true;
		executor.runUntilIdle();
		

		verify(httpResponse).header(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
		verify(httpResponse).header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE);
		verify(httpResponse).header(HttpHeaders.Names.CONTENT_TYPE, MimeTypes.get(".html"));
		verify(httpResponse).content(bytes);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testWaitsForInitialization() throws Exception {
		
		DocumentRequestProcessor toTest = toTest(Collections.<DocumentFilter>emptySet());
		given(documentScriptEnvironment.hasServerScript()).willReturn(true);
		
		toTest.process();
		executor.isScriptThread = true;
		executor.runUntilIdle();
		
		verify(initializer).executeOnInitialization(eq(documentScriptEnvironment), any(ScriptTask.class));
		
		verifyZeroInteractions(continuationCoordinator);
	}
	
	@Test
	public void testInvokesReadyFunctionWithInitializedScriptAndContinuation() throws Exception {
		
		// given
		DocumentRequestProcessor toTest = toTest(Collections.<DocumentFilter>emptySet());
		given(documentScriptEnvironment.hasServerScript()).willReturn(true);
		given(documentScriptEnvironment.initialized()).willReturn(true);
		given(documentScriptEnvironment.getFunction(DocumentScriptEnvironment.READY_FUNCTION_KEY)).willReturn(callable);
		given(continuationCoordinator.execute(documentScriptEnvironment, callable)).willReturn(pendingKey);
		
		toTest.process();
		
		executor.isScriptThread = true;
		@SuppressWarnings("unchecked")
		ScriptTask<? extends ScriptEnvironment> task = (ScriptTask<? extends ScriptEnvironment>)executor.firstTask();
		executor.runUntilIdle();
		
		assertThat(TaskHelper.pendingKey(task), is(pendingKey));
		Object result = new Object();
		TaskHelper.resumeWith(task, result);
		
		TaskHelper.invoke(task);
		
		verify(continuationCoordinator).resumeContinuation(documentScriptEnvironment, pendingKey, result);
		
		verify(httpResponse).header(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
		verify(httpResponse).header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE);
		verify(httpResponse).header(HttpHeaders.Names.CONTENT_TYPE, MimeTypes.get(".html"));
		verify(httpResponse).content(bytes);
		
		verifyZeroInteractions(initializer);
	}
	
	@Test
	public void testInvokesReadyFunctionWithInitializedScript() throws Exception {
		
		// given
		DocumentRequestProcessor toTest = toTest(Collections.<DocumentFilter>emptySet());
		given(documentScriptEnvironment.hasServerScript()).willReturn(true);
		given(documentScriptEnvironment.initialized()).willReturn(true);
		given(documentScriptEnvironment.getFunction(DocumentScriptEnvironment.READY_FUNCTION_KEY)).willReturn(callable);
		
		toTest.process();
		
		executor.isScriptThread = true;
		executor.runUntilIdle();
		
		verify(continuationCoordinator).execute(documentScriptEnvironment, callable);
		
		verify(httpResponse).header(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
		verify(httpResponse).header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE);
		verify(httpResponse).header(HttpHeaders.Names.CONTENT_TYPE, MimeTypes.get(".html"));
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
		executor.isScriptThread = true;
		toTest.respond();
		executor.runUntilIdle();

		// then
		assertThat(filterCalls, is(6));
		// TODO can make this smarter later
	}
	
	@Test
	public void testWritesDocumentCorrectly() throws Exception {
		// given
		DocumentRequestProcessor toTest = toTest(Collections.<DocumentFilter>emptySet());
				
		
		executor.isScriptThread = true;
		
		// when
		toTest.respond();
		executor.runUntilIdle();
		
		// then
		verify(httpResponse).header(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
		verify(httpResponse).header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE);
		verify(httpResponse).header(HttpHeaders.Names.CONTENT_TYPE, MimeTypes.get(".html"));
		verify(httpResponse).content(bytes);
	}

}
