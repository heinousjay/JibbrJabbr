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
import jj.execution.TaskHelper;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.resource.MimeTypes;
import jj.script.ContinuationCoordinator;
import jj.script.ContinuationPendingKey;
import jj.script.DependsOnScriptEnvironmentInitialization;
import jj.script.ScriptEnvironment;
import jj.script.ScriptTask;
import jj.script.ScriptTaskHelper;
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
	
	MockTaskRunner taskRunner;
	
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

		taskRunner = new MockTaskRunner();
		
		given(documentScriptEnvironment.name()).willReturn(baseName);
		given(documentScriptEnvironment.document()).willReturn(document);
		
		given(httpRequest.uri()).willReturn("/");
		
		// auto-stubbing the builder pattern
		httpResponse = mock(HttpResponse.class, ANSWER_WITH_SELF);
		
		pendingKey = new ContinuationPendingKey();
		
		currentDocument = new CurrentDocumentRequestProcessor();
	}
	
	private DocumentRequestProcessor toTest(Set<DocumentFilter> filters) {
		return new DocumentRequestProcessor(
			taskRunner,
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
		
		assertThat(taskRunner.tasks.size(), is(1));
		assertThat(taskRunner.tasks.get(0), is(instanceOf(ScriptTask.class)));
		taskRunner.runUntilIdle();
		

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
		taskRunner.runUntilIdle();
		
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
		
		@SuppressWarnings("unchecked")
		ScriptTask<? extends ScriptEnvironment> task = (ScriptTask<? extends ScriptEnvironment>)taskRunner.firstTask();
		taskRunner.runUntilIdle();
		
		assertThat(ScriptTaskHelper.pendingKey(task), is(pendingKey));
		Object result = new Object();
		ScriptTaskHelper.resumeWith(task, result);
		
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
		
		taskRunner.runUntilIdle();
		
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
		verify(httpResponse).header(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
		verify(httpResponse).header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE);
		verify(httpResponse).header(HttpHeaders.Names.CONTENT_TYPE, MimeTypes.get(".html"));
		verify(httpResponse).content(bytes);
	}

}
