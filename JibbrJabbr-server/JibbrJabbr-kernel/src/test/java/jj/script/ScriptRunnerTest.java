package jj.script;

import static org.mockito.BDDMockito.*;

import java.util.HashSet;
import java.util.Set;

import jj.document.DocumentRequestProcessor;
import jj.execution.ScriptExecutorFactory;
import jj.hostapi.HostEvent;
import jj.http.HttpRequestState;
import jj.http.JJWebSocketConnection;
import jj.http.MockHttpRequest;
import jj.resource.ScriptResource;

import org.jmock.lib.concurrent.DeterministicScheduler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Callable;

@RunWith(MockitoJUnitRunner.class)
public class ScriptRunnerTest {
	
	// it takes a village to isolate the ScriptRunner.  it
	// sits on the top of a serious mountain of man-hours.
	
	String baseName = "index";
	
	Document document;
	
	@Mock AssociatedScriptBundle associatedScriptBundle;
	
	@Mock ModuleScriptBundle moduleScriptBundle;
		
	@Mock ScriptResource scriptResource;
	
	@Mock ScriptBundleHelper scriptBundleHelper;
	
	@Mock ContinuationCoordinator continuationCoordinator;
	
	@Mock ContinuationState continuationState;
	
	@Mock CurrentScriptContext currentScriptContext;
	
	@Mock ScriptExecutorFactory scriptExecutorFactory;
	
	DeterministicScheduler executor;
	
	@Spy MockHttpRequest httpRequest;
	
	ScriptRunner scriptRunner;
	
	@Mock DocumentRequestProcessor documentRequestProcessor;
	
	@Mock JJWebSocketConnection connection;
	
	@Mock Callable eventFunction;
	
	@Mock ContinuationProcessor continuationProcessor1;
	
	@Mock ContinuationProcessor continuationProcessor2;
	
	@Mock ContinuationProcessor continuationProcessor3;
	
	ScriptContext httpRequestContext;
	
	@Before
	public void before() {
		
		when(scriptBundleHelper.scriptBundleFor(baseName)).thenReturn(associatedScriptBundle);
		
		when(currentScriptContext.associatedScriptBundle()).thenReturn(associatedScriptBundle);
		
		executor = new DeterministicScheduler();
		when(scriptExecutorFactory.executorFor(baseName)).thenReturn(executor);
		
		when(continuationProcessor1.type()).thenReturn(ContinuationType.AsyncHttpRequest);
		when(continuationProcessor2.type()).thenReturn(ContinuationType.JJMessage);
		when(continuationProcessor3.type()).thenReturn(ContinuationType.RequiredModule);
		
		Set<ContinuationProcessor> continuationProcessors = new HashSet<>();
		continuationProcessors.add(continuationProcessor1);
		continuationProcessors.add(continuationProcessor2);
		continuationProcessors.add(continuationProcessor3);
		
		scriptRunner = new ScriptRunner(
			scriptBundleHelper,
			continuationCoordinator,
			currentScriptContext,
			scriptExecutorFactory,
			continuationProcessors
		);
		
		document = Jsoup.parse("<html><head><title>what</title></head><body></body></html>");
		
		when(documentRequestProcessor.baseName()).thenReturn(baseName);
		when(documentRequestProcessor.document()).thenReturn(document);
		
		
		when(associatedScriptBundle.getFunction(ScriptRunner.READY_FUNCTION_KEY)).thenReturn(eventFunction);
		
		httpRequestContext = new ScriptContext(null, documentRequestProcessor);
	}
	
	private void givenADocumentRequest() {

		given(currentScriptContext.httpRequest()).willReturn(httpRequest);
		given(currentScriptContext.documentRequestProcessor()).willReturn(documentRequestProcessor);
		given(currentScriptContext.type()).willReturn(ScriptContextType.HttpRequest);
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
		
		given(httpRequest.state()).willReturn(HttpRequestState.InitialExecution);
		
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
		given(continuationCoordinator.execute(associatedScriptBundle, eventFunction))
			.willReturn(continuationState);
		
		given(continuationCoordinator.execute(associatedScriptBundle, eventFunction))
			.willReturn(continuationState);
		given(continuationState.type()).willReturn(ContinuationType.AsyncHttpRequest);
		
		given(httpRequest.state()).willReturn(HttpRequestState.ReadyFunctionExecution);
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
		given(connection.getFunction(any(String.class))).willReturn(eventFunction);
		given(connection.associatedScriptBundle()).willReturn(associatedScriptBundle);
		given(currentScriptContext.connection()).willReturn(connection);
		given(currentScriptContext.type()).willReturn(ScriptContextType.WebSocket);
		
	}
	
	@Test
	public void testWebSocketHostEventWithNoContinuation() {
		
		// given
		givenAWebSocketMessage();
		
		// when
		scriptRunner.submit(connection, HostEvent.clientConnected, connection);
		executor.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(associatedScriptBundle, eventFunction, connection);
	}
	
	@Test
	public void testWebSocketHostEventWithContinuations() {
		
		// given
		givenAWebSocketMessage();
		given(currentScriptContext.scriptBundle()).willReturn(associatedScriptBundle);
		given(continuationState.type()).willReturn(ContinuationType.AsyncHttpRequest);
		given(continuationCoordinator.execute(associatedScriptBundle, eventFunction, connection)).willReturn(continuationState);
		given(continuationCoordinator.resumeContinuation((String)any(), (ScriptBundle)any(), any()))
			.willReturn(continuationState)
			.willReturn(null);
		
		// when
		scriptRunner.submit(connection, HostEvent.clientConnected, connection);
		executor.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(associatedScriptBundle, eventFunction, connection);
		
		// given
		given(scriptExecutorFactory.isScriptThread()).willReturn(true);
		
		// when
		scriptRunner.restartAfterContinuation(null, null);
		scriptRunner.restartAfterContinuation(null, null);
		
		// then
		verify(continuationCoordinator, times(2)).resumeContinuation((String)any(), (ScriptBundle)any(), any());
	}
	
	@Test
	public void testWebSocketEventDelegationToGlobal() {
		
		// given 
		givenAWebSocketMessage();
		String eventName = EventNameHelper.makeEventName("jason", "miller", "rules");
		given(associatedScriptBundle.getFunction(eventName)).willReturn(eventFunction);
		given(connection.getFunction(eventName)).willReturn(null);
		
		// when
		scriptRunner.submit(connection, eventName);
		executor.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(associatedScriptBundle, eventFunction);
		verify(connection).getFunction(eventName);
		verify(associatedScriptBundle).getFunction(eventName);

		//given
		eventName = EventNameHelper.makeEventName("jason", "miller", "rocks");
		given(associatedScriptBundle.getFunction(eventName)).willReturn(null);
		given(connection.getFunction(eventName)).willReturn(eventFunction);
		
		// when
		scriptRunner.submit(connection, eventName);
		executor.runUntilIdle();
		
		// then
		verify(continuationCoordinator, times(2)).execute(associatedScriptBundle, eventFunction);
		verify(connection).getFunction(eventName);
		verify(associatedScriptBundle, never()).getFunction(eventName);
		
	}
	
	@Test
	public void testWebSocketJJMessageEventWithNoContinuation() {
		
		// given
		givenAWebSocketMessage();
		String eventName = EventNameHelper.makeEventName("jason", "miller", "rules");
		
		// when
		scriptRunner.submit(connection, eventName);
		executor.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(associatedScriptBundle, eventFunction);
	}
	
	@Test
	public void testWebSocketJJMessageEventWithContinuations() {
		
		// given
		givenAWebSocketMessage();
		given(currentScriptContext.scriptBundle()).willReturn(associatedScriptBundle);
		
		String eventName = EventNameHelper.makeEventName("jason", "miller", "rules");
		given(continuationState.type()).willReturn(ContinuationType.JJMessage);
		given(continuationCoordinator.execute(associatedScriptBundle, eventFunction)).willReturn(continuationState);
		
		given(continuationCoordinator.resumeContinuation((String)any(), (ScriptBundle)any(), any()))
			.willReturn(continuationState)
			.willReturn(null);
		
		// when
		scriptRunner.submit(connection, eventName);
		executor.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(associatedScriptBundle, eventFunction);
		
		// given
		given(scriptExecutorFactory.isScriptThread()).willReturn(true);
		
		// when
		scriptRunner.restartAfterContinuation(null, null);
		scriptRunner.restartAfterContinuation(null, null);
		
		// then
		verify(continuationCoordinator, times(2)).resumeContinuation((String)any(), (ScriptBundle)any(), any());
	}
	
	@Test
	public void testWebSocketJJMessageResultWithNoContinuation() {
		
		// given
		final String key = "0";
		final String value = "value";
		givenAWebSocketMessage();
		
		// when
		scriptRunner.submitPendingResult(connection, key, value);
		executor.runUntilIdle();
		
		// then
		verify(continuationCoordinator).resumeContinuation(key, null, value);
	}
	
	private RequiredModule givenAModuleRequire() {
		given(moduleScriptBundle.initialized()).willReturn(false);
		given(moduleScriptBundle.baseName()).willReturn(baseName);
		given(currentScriptContext.baseName()).willReturn(baseName);
		given(currentScriptContext.scriptBundle()).willReturn(moduleScriptBundle);
		given(currentScriptContext.moduleScriptBundle()).willReturn(moduleScriptBundle);
		given(scriptBundleHelper.scriptBundleFor(baseName, "module")).willReturn(moduleScriptBundle);
		
		RequiredModule requiredModule = new RequiredModule("module", currentScriptContext);
		given(currentScriptContext.requiredModule()).willReturn(requiredModule);
		return requiredModule;
	}
	
	@Test
	public void testModuleScriptWithNoContinuation() {
		
		// given
		RequiredModule module = givenAModuleRequire();
		
		// when
		scriptRunner.submit(module);
		executor.runNextPendingCommand();
		
		// then
		verify(continuationCoordinator).execute(moduleScriptBundle);
		
		// given
		given(currentScriptContext.type()).willReturn(ScriptContextType.HttpRequest);
		given(scriptExecutorFactory.isScriptThread()).willReturn(true);
		given(currentScriptContext.httpRequest()).willReturn(httpRequest);
		given(httpRequest.state()).willReturn(HttpRequestState.Uninitialized);
		
		// when
		executor.runUntilIdle();
		
		// then
		verify(moduleScriptBundle).initialized(true);
		verify(continuationCoordinator).resumeContinuation(module.pendingKey(), moduleScriptBundle, null);
	}
	
	@Test
	public void testModuleScriptWithContinuation() {
		
		// given
		RequiredModule module = givenAModuleRequire();
		given(continuationCoordinator.execute(moduleScriptBundle)).willReturn(continuationState);
		given(continuationState.type()).willReturn(ContinuationType.JJMessage);
		
		// when
		scriptRunner.submit(module);
		executor.runNextPendingCommand();
		
		// then
		verify(continuationCoordinator).execute(moduleScriptBundle);
		
		// given
		given(scriptExecutorFactory.isScriptThread()).willReturn(true);
		given(currentScriptContext.type()).willReturn(ScriptContextType.ModuleInitialization);
		given(continuationCoordinator.execute(moduleScriptBundle)).willReturn(null);
		
		// when
		scriptRunner.restartAfterContinuation("blah", null);
		executor.runNextPendingCommand();
		
		// then
		verify(continuationCoordinator).resumeContinuation("blah", moduleScriptBundle, null);
		
		// given
		given(currentScriptContext.httpRequest()).willReturn(httpRequest);
		given(httpRequest.state()).willReturn(HttpRequestState.Uninitialized);
		
		// when
		executor.runNextPendingCommand();
	}

}
