package jj.script;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.IOException;

import jj.request.DocumentRequestProcessor;
import jj.resource.ResourceFinder;
import jj.resource.ScriptResource;
import jj.resource.ScriptResourceType;
import jj.webbit.JJHttpRequest;
import jj.webbit.JJHttpRequest.State;

import org.jmock.lib.concurrent.DeterministicScheduler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Callable;

@RunWith(MockitoJUnitRunner.class)
public class ScriptRunnerTest {
	
	// it takes a village to isolate the ScriptRunner.  it
	// sits on the top of a serious mountain of man-hours.
	
	String baseName = "index";
	
	Document document;
	
	@Mock ScriptBundle scriptBundle;
	
	ScriptBundles scriptBundles;
	
	@Mock ScriptBundleCreator scriptBundleCreator;
	
	@Mock ScriptResource scriptResource;
	
	@Mock ResourceFinder resourceFinder;
	
	@Mock ContinuationCoordinator continuationCoordinator;
	
	@Mock ContinuationState continuationState;
	
	@Mock CurrentScriptContext currentScriptContext;
	
	@Mock ScriptExecutorFactory scriptExecutorFactory;
	
	DeterministicScheduler executor;
	
	@Mock JJHttpRequest httpRequest;
	
	ScriptRunner scriptRunner;
	
	@Mock DocumentRequestProcessor documentRequestProcessor;
	
	@Mock Callable readyFunction;
	
	@Mock ContinuationProcessor continuationProcessor;
	
	@Before
	public void before() throws IOException {
		
		// no need to mock this one, it's just a HashMap
		scriptBundles = new ScriptBundles();
		
		when(scriptBundleCreator.createScriptBundle(
				null,
				scriptResource,
				scriptResource,
				scriptResource,
				baseName
			)).thenReturn(scriptBundle);
		
		when(currentScriptContext.scriptBundle()).thenReturn(scriptBundle);
		
		executor = new DeterministicScheduler();
		when(scriptExecutorFactory.executorFor(baseName)).thenReturn(executor);
		
		when(continuationProcessor.type()).thenReturn(ContinuationType.AsyncHttpRequest);
		
		scriptRunner = new ScriptRunner(
			scriptBundles,
			scriptBundleCreator,
			resourceFinder,
			continuationCoordinator,
			currentScriptContext,
			scriptExecutorFactory,
			new ContinuationProcessor[] { continuationProcessor }
		);
		
		document = Jsoup.parse("<html><head><title>what</title></head><body></body></html>");
		
		when(documentRequestProcessor.baseName()).thenReturn(baseName);
		when(documentRequestProcessor.document()).thenReturn(document);

		when(currentScriptContext.httpRequest()).thenReturn(httpRequest);
		when(currentScriptContext.documentRequestProcessor()).thenReturn(documentRequestProcessor);
		
		when(scriptBundle.getFunction(ScriptRunner.READY_FUNCTION_KEY)).thenReturn(readyFunction);
	}
	
	//@Test
	public void testInitialDocumentRequestWithNoContinuations() throws IOException {
		
		// given
		given(resourceFinder.loadResource(ScriptResource.class, baseName, ScriptResourceType.Client)).willReturn(scriptResource);
		given(resourceFinder.loadResource(ScriptResource.class, baseName, ScriptResourceType.Shared)).willReturn(scriptResource);
		given(resourceFinder.loadResource(ScriptResource.class, baseName, ScriptResourceType.Server)).willReturn(scriptResource);
		
		// when
		scriptRunner.submit(documentRequestProcessor);
		executor.runUntilIdle();
		
		// then
		assertThat(scriptBundles.get(baseName), is(scriptBundle));
		verify(httpRequest).startingInitialExecution();
		verify(httpRequest).startingReadyFunction();
		verify(documentRequestProcessor).respond();
	}
	
	@Test
	public void testInitialDocumentRequestWithRESTContinuationDuringInitialization() throws IOException {
		
		// given
		given(resourceFinder.loadResource(ScriptResource.class, baseName, ScriptResourceType.Client)).willReturn(scriptResource);
		given(resourceFinder.loadResource(ScriptResource.class, baseName, ScriptResourceType.Shared)).willReturn(scriptResource);
		given(resourceFinder.loadResource(ScriptResource.class, baseName, ScriptResourceType.Server)).willReturn(scriptResource);
		
		given(continuationCoordinator.execute(scriptBundle)).willReturn(continuationState);
		given(continuationState.type()).willReturn(ContinuationType.AsyncHttpRequest);
		
		given(httpRequest.state()).willReturn(State.InitialExecution);
		
		// when
		scriptRunner.submit(documentRequestProcessor);
		executor.runUntilIdle();
		
		// then
		assertThat(scriptBundles.get(baseName), is(scriptBundle)); // verifies script compilation
		verify(httpRequest).startingInitialExecution();
		verify(continuationProcessor).process(continuationState);
		
		// when
		scriptRunner.restartAfterContinuation("", null);
		
		// then
		verify(httpRequest).startingReadyFunction();
		verify(documentRequestProcessor).respond(); // verifies execution processing
	}
	
	@Test
	public void testInitialDocumentRequestWithRESTContinuationDuringReadyFunction() throws IOException {
		
		// given
		given(resourceFinder.loadResource(ScriptResource.class, baseName, ScriptResourceType.Client)).willReturn(scriptResource);
		given(resourceFinder.loadResource(ScriptResource.class, baseName, ScriptResourceType.Shared)).willReturn(scriptResource);
		given(resourceFinder.loadResource(ScriptResource.class, baseName, ScriptResourceType.Server)).willReturn(scriptResource);
		
		given(continuationCoordinator.execute(scriptBundle, ScriptRunner.READY_FUNCTION_KEY))
			.willReturn(continuationState);
		
		given(continuationCoordinator.execute(scriptBundle, ScriptRunner.READY_FUNCTION_KEY))
			.willReturn(continuationState);
		given(continuationState.type()).willReturn(ContinuationType.AsyncHttpRequest);
		
		given(httpRequest.state()).willReturn(State.ReadyFunctionExecution);
		
		// when
		scriptRunner.submit(documentRequestProcessor);
		executor.runUntilIdle();
		
		// then
		assertThat(scriptBundles.get(baseName), is(scriptBundle)); // verifies script compilation
		verify(httpRequest).startingInitialExecution();
		verify(httpRequest).startingReadyFunction();
		verify(continuationProcessor).process(continuationState);
		
		// when
		scriptRunner.restartAfterContinuation("", null);
		
		// then
		verify(documentRequestProcessor).respond(); // verifies execution processing
	}

}
