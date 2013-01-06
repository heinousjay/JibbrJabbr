package jj.script;

import static org.mockito.BDDMockito.*;

import jj.document.DocumentRequestProcessor;
import jj.hostapi.HostEvent;
import jj.jqmessage.JQueryMessage;
import jj.jqmessage.MessageMaker;
import jj.resource.ScriptResource;
import jj.webbit.JJHttpRequest;
import jj.webbit.JJHttpRequest.State;
import jj.webbit.JJWebSocketConnection;

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
	
	@Mock AssociatedScriptBundle associatedScriptBundle;
		
	@Mock ScriptResource scriptResource;
	
	@Mock ScriptBundleHelper scriptBundleHelper;
	
	@Mock ContinuationCoordinator continuationCoordinator;
	
	@Mock ContinuationState continuationState;
	
	@Mock CurrentScriptContext currentScriptContext;
	
	@Mock ScriptExecutorFactory scriptExecutorFactory;
	
	DeterministicScheduler executor;
	
	@Mock JJHttpRequest httpRequest;
	
	ScriptRunner scriptRunner;
	
	@Mock DocumentRequestProcessor documentRequestProcessor;
	
	@Mock JJWebSocketConnection connection;
	
	@Mock Callable readyFunction;
	
	@Mock ContinuationProcessor continuationProcessor1;
	
	@Mock ContinuationProcessor continuationProcessor2;
	
	@Before
	public void before() {
		
		when(scriptBundleHelper.scriptBundleFor(baseName)).thenReturn(associatedScriptBundle);
		
		when(currentScriptContext.associatedScriptBundle()).thenReturn(associatedScriptBundle);
		
		executor = new DeterministicScheduler();
		when(scriptExecutorFactory.executorFor(baseName)).thenReturn(executor);
		
		when(continuationProcessor1.type()).thenReturn(ContinuationType.AsyncHttpRequest);
		when(continuationProcessor2.type()).thenReturn(ContinuationType.JQueryMessage);
		
		scriptRunner = new ScriptRunner(
			scriptBundleHelper,
			continuationCoordinator,
			currentScriptContext,
			scriptExecutorFactory,
			new ContinuationProcessor[] { continuationProcessor1, continuationProcessor2 }
		);
		
		document = Jsoup.parse("<html><head><title>what</title></head><body></body></html>");
		
		when(documentRequestProcessor.baseName()).thenReturn(baseName);
		when(documentRequestProcessor.document()).thenReturn(document);
		
		
		when(associatedScriptBundle.getFunction(ScriptRunner.READY_FUNCTION_KEY)).thenReturn(readyFunction);
	}
	
	private void givenADocumentRequest() {

		given(currentScriptContext.httpRequest()).willReturn(httpRequest);
		given(currentScriptContext.documentRequestProcessor()).willReturn(documentRequestProcessor);
	}
	
	@Test
	public void testDocumentWithNoScript() {
		
		// given
		given(scriptBundleHelper.scriptBundleFor(baseName)).willReturn(null);
		givenADocumentRequest();
		
		// when
		scriptRunner.submit(documentRequestProcessor);
		executor.runUntilIdle();
		
		// then
		verify(documentRequestProcessor).respond();
	}
	
	@Test
	public void testInitialDocumentRequestWithNoContinuations() {
		
		// given
		givenADocumentRequest();
		
		// when
		scriptRunner.submit(documentRequestProcessor);
		executor.runUntilIdle();
		
		// then
		verify(httpRequest).startingInitialExecution();
		verify(httpRequest).startingReadyFunction();
		verify(documentRequestProcessor).respond();
	}
	
	@Test
	public void testInitialDocumentRequestWithRESTContinuationDuringInitialization() {
		
		// given
		given(currentScriptContext.scriptBundle()).willReturn(associatedScriptBundle);
		given(continuationCoordinator.execute(associatedScriptBundle)).willReturn(continuationState);
		given(continuationState.type()).willReturn(ContinuationType.AsyncHttpRequest);
		givenADocumentRequest();
		
		given(httpRequest.state()).willReturn(State.InitialExecution);
		
		// when
		scriptRunner.submit(documentRequestProcessor);
		executor.runUntilIdle();
		
		// then
		verify(httpRequest).startingInitialExecution();
		verify(continuationProcessor1).process(continuationState);
		
		// given
		given(scriptExecutorFactory.isScriptThread()).willReturn(true);
		
		// when
		scriptRunner.restartAfterContinuation("", null);
		
		// then
		verify(httpRequest).startingReadyFunction();
		verify(documentRequestProcessor).respond(); // verifies execution processing
	}
	
	@Test
	public void testInitialDocumentRequestWithRESTContinuationDuringReadyFunction() {
		
		// given
		given(currentScriptContext.scriptBundle()).willReturn(associatedScriptBundle);
		given(continuationCoordinator.execute(associatedScriptBundle, ScriptRunner.READY_FUNCTION_KEY))
			.willReturn(continuationState);
		
		given(continuationCoordinator.execute(associatedScriptBundle, ScriptRunner.READY_FUNCTION_KEY))
			.willReturn(continuationState);
		given(continuationState.type()).willReturn(ContinuationType.AsyncHttpRequest);
		
		given(httpRequest.state()).willReturn(State.ReadyFunctionExecution);
		givenADocumentRequest();
		
		// when
		scriptRunner.submit(documentRequestProcessor);
		executor.runUntilIdle();
		
		// then
		verify(httpRequest).startingInitialExecution();
		verify(httpRequest).startingReadyFunction();
		verify(continuationProcessor1).process(continuationState);
		
		// given
		given(scriptExecutorFactory.isScriptThread()).willReturn(true);
		
		// when
		scriptRunner.restartAfterContinuation("", null);
		
		// then
		verify(documentRequestProcessor).respond(); // verifies execution processing
	}
	
	private void givenAWebSocketMessage() {

		given(connection.baseName()).willReturn(baseName);
		given(connection.associatedScriptBundle()).willReturn(associatedScriptBundle);
		given(currentScriptContext.connection()).willReturn(connection);
		
	}
	
	@Test
	public void testWebSocketHostEventWithNoContinuation() {
		
		// given
		givenAWebSocketMessage();
		
		// when
		scriptRunner.submit(connection, HostEvent.clientConnected, connection);
		executor.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(associatedScriptBundle, HostEvent.clientConnected.toString(), connection);
	}
	
	@Test
	public void testWebSocketHostEventWithContinuations() {
		
		// given
		givenAWebSocketMessage();
		given(currentScriptContext.scriptBundle()).willReturn(associatedScriptBundle);
		given(continuationState.type()).willReturn(ContinuationType.AsyncHttpRequest);
		given(continuationCoordinator.execute(associatedScriptBundle, HostEvent.clientConnected.toString(), connection)).willReturn(continuationState);
		given(continuationCoordinator.resumeContinuation((String)any(), (ScriptBundle)any(), any()))
			.willReturn(continuationState)
			.willReturn(null);
		
		// when
		scriptRunner.submit(connection, HostEvent.clientConnected, connection);
		executor.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(associatedScriptBundle, HostEvent.clientConnected.toString(), connection);
		
		// given
		given(scriptExecutorFactory.isScriptThread()).willReturn(true);
		
		// when
		scriptRunner.restartAfterContinuation(null, null);
		scriptRunner.restartAfterContinuation(null, null);
		
		// then
		verify(continuationCoordinator, times(2)).resumeContinuation((String)any(), (ScriptBundle)any(), any());
	}
	
	@Test
	public void testWebSocketJQueryMessageEventWithNoContinuation() {
		
		// given
		givenAWebSocketMessage();
		JQueryMessage jwm = MessageMaker.makeEvent("jason", "miller");
		String eventName = EventNameHelper.makeEventName(jwm);
		
		// when
		scriptRunner.submit(connection, jwm);
		executor.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(associatedScriptBundle, eventName);
	}
	
	@Test
	public void testWebSocketJQueryMessageEventWithContinuations() {
		
		// given
		givenAWebSocketMessage();
		given(currentScriptContext.scriptBundle()).willReturn(associatedScriptBundle);
		JQueryMessage jwm = MessageMaker.makeEvent("jason", "miller");
		String eventName = EventNameHelper.makeEventName(jwm);
		given(continuationState.type()).willReturn(ContinuationType.JQueryMessage);
		given(continuationCoordinator.execute(associatedScriptBundle, eventName)).willReturn(continuationState);
		given(continuationCoordinator.resumeContinuation((String)any(), (ScriptBundle)any(), any()))
			.willReturn(continuationState)
			.willReturn(null);
		
		// when
		scriptRunner.submit(connection, jwm);
		executor.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(associatedScriptBundle, eventName);
		
		// given
		given(scriptExecutorFactory.isScriptThread()).willReturn(true);
		
		// when
		scriptRunner.restartAfterContinuation(null, null);
		scriptRunner.restartAfterContinuation(null, null);
		
		// then
		verify(continuationCoordinator, times(2)).resumeContinuation((String)any(), (ScriptBundle)any(), any());
	}

}
