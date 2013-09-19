package jj.script;

import static org.mockito.BDDMockito.*;

import java.util.HashMap;
import java.util.Map;

import jj.engine.HostEvent;
import jj.execution.ScriptExecutorFactory;
import jj.http.HttpRequest;
import jj.http.server.JJWebSocketConnection;
import jj.http.server.servable.document.DocumentRequestProcessor;
import jj.http.server.servable.document.DocumentRequestState;
import jj.resource.document.ScriptResource;

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
	
	@Mock DocumentScriptExecutionEnvironment associatedScriptExecutionEnvironment;
	
	@Mock ModuleScriptExecutionEnvironment moduleScriptExecutionEnvironment;
		
	@Mock ScriptResource scriptResource;
	
	@Mock ScriptExecutionEnvironmentHelper scriptExecutionEnvironmentHelper;
	
	@Mock ContinuationCoordinator continuationCoordinator;
	
	@Mock ContinuationState continuationState;
	
	@Mock CurrentScriptContext currentScriptContext;
	
	@Mock ScriptExecutorFactory scriptExecutorFactory;
	
	DeterministicScheduler executor;
	
	@Mock HttpRequest httpRequest;
	
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
		
		when(scriptExecutionEnvironmentHelper.scriptExecutionEnvironmentFor(baseName)).thenReturn(associatedScriptExecutionEnvironment);
		
		when(currentScriptContext.documentScriptExecutionEnvironment()).thenReturn(associatedScriptExecutionEnvironment);
		
		executor = new DeterministicScheduler();
		when(scriptExecutorFactory.executorFor(baseName)).thenReturn(executor);
		
		Map<ContinuationType, ContinuationProcessor> continuationProcessors = new HashMap<>();
		continuationProcessors.put(ContinuationType.AsyncHttpRequest, continuationProcessor1);
		continuationProcessors.put(ContinuationType.JJMessage, continuationProcessor2);
		continuationProcessors.put(ContinuationType.RequiredModule, continuationProcessor3);
		
		scriptRunner = new ScriptRunner(
			scriptExecutionEnvironmentHelper,
			continuationCoordinator,
			currentScriptContext,
			scriptExecutorFactory,
			continuationProcessors
		);
		
		document = Jsoup.parse("<html><head><title>what</title></head><body></body></html>");
		
		when(documentRequestProcessor.baseName()).thenReturn(baseName);
		when(documentRequestProcessor.document()).thenReturn(document);
		
		
		when(associatedScriptExecutionEnvironment.getFunction(ScriptRunner.READY_FUNCTION_KEY)).thenReturn(eventFunction);
		
		httpRequestContext = new ScriptContext(null, documentRequestProcessor);
	}
	
	private void givenADocumentRequest() {

		given(currentScriptContext.httpRequest()).willReturn(httpRequest);
		given(currentScriptContext.documentRequestProcessor()).willReturn(documentRequestProcessor);
		given(currentScriptContext.type()).willReturn(ScriptContextType.DocumentRequest);
	}
	
	@Test
	public void testDocumentWithNoScript() {
		
		// given
		given(scriptExecutionEnvironmentHelper.scriptExecutionEnvironmentFor(baseName)).willReturn(null);
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
		verify(documentRequestProcessor).startingInitialExecution();
		verify(documentRequestProcessor).startingReadyFunction();
		verify(documentRequestProcessor).respond();
	}
	
	@Test
	public void testInitialDocumentRequestWithRESTContinuationDuringInitialization() {
		
		// given
		given(currentScriptContext.scriptExecutionEnvironment()).willReturn(associatedScriptExecutionEnvironment);
		given(continuationCoordinator.execute(associatedScriptExecutionEnvironment)).willReturn(continuationState);
		given(continuationState.type()).willReturn(ContinuationType.AsyncHttpRequest);
		givenADocumentRequest();
		
		given(documentRequestProcessor.state()).willReturn(DocumentRequestState.InitialExecution);
		
		// when
		scriptRunner.submit(documentRequestProcessor);
		executor.runUntilIdle();
		
		// then
		verify(documentRequestProcessor).startingInitialExecution();
		verify(continuationProcessor1).process(continuationState);
		
		// given
		given(scriptExecutorFactory.isScriptThread()).willReturn(true);
		
		// when
		scriptRunner.restartAfterContinuation("", null);
		
		// then
		verify(documentRequestProcessor).startingReadyFunction();
		verify(documentRequestProcessor).respond(); // verifies execution processing
	}
	
	@Test
	public void testInitialDocumentRequestWithRESTContinuationDuringReadyFunction() {
		
		// given
		given(currentScriptContext.scriptExecutionEnvironment()).willReturn(associatedScriptExecutionEnvironment);
		given(continuationCoordinator.execute(associatedScriptExecutionEnvironment, eventFunction))
			.willReturn(continuationState);
		
		given(continuationCoordinator.execute(associatedScriptExecutionEnvironment, eventFunction))
			.willReturn(continuationState);
		given(continuationState.type()).willReturn(ContinuationType.AsyncHttpRequest);
		
		given(documentRequestProcessor.state()).willReturn(DocumentRequestState.ReadyFunctionExecution);
		givenADocumentRequest();
		
		// when
		scriptRunner.submit(documentRequestProcessor);
		executor.runUntilIdle();
		
		// then
		verify(documentRequestProcessor).startingInitialExecution();
		verify(documentRequestProcessor).startingReadyFunction();
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
		given(connection.associatedScriptExecutionEnvironment()).willReturn(associatedScriptExecutionEnvironment);
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
		verify(continuationCoordinator).execute(associatedScriptExecutionEnvironment, eventFunction, connection);
	}
	
	@Test
	public void testWebSocketHostEventWithContinuations() {
		
		// given
		givenAWebSocketMessage();
		given(currentScriptContext.scriptExecutionEnvironment()).willReturn(associatedScriptExecutionEnvironment);
		given(continuationState.type()).willReturn(ContinuationType.AsyncHttpRequest);
		given(continuationCoordinator.execute(associatedScriptExecutionEnvironment, eventFunction, connection)).willReturn(continuationState);
		given(continuationCoordinator.resumeContinuation((String)any(), (ScriptExecutionEnvironment)any(), any()))
			.willReturn(continuationState)
			.willReturn(null);
		
		// when
		scriptRunner.submit(connection, HostEvent.clientConnected, connection);
		executor.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(associatedScriptExecutionEnvironment, eventFunction, connection);
		
		// given
		given(scriptExecutorFactory.isScriptThread()).willReturn(true);
		
		// when
		scriptRunner.restartAfterContinuation(null, null);
		scriptRunner.restartAfterContinuation(null, null);
		
		// then
		verify(continuationCoordinator, times(2)).resumeContinuation((String)any(), (ScriptExecutionEnvironment)any(), any());
	}
	
	@Test
	public void testWebSocketEventDelegationToGlobal() {
		
		// given 
		givenAWebSocketMessage();
		String eventName = EventNameHelper.makeEventName("jason", "miller", "rules");
		given(associatedScriptExecutionEnvironment.getFunction(eventName)).willReturn(eventFunction);
		given(connection.getFunction(eventName)).willReturn(null);
		
		// when
		scriptRunner.submit(connection, eventName);
		executor.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(associatedScriptExecutionEnvironment, eventFunction);
		verify(connection).getFunction(eventName);
		verify(associatedScriptExecutionEnvironment).getFunction(eventName);

		//given
		eventName = EventNameHelper.makeEventName("jason", "miller", "rocks");
		given(associatedScriptExecutionEnvironment.getFunction(eventName)).willReturn(null);
		given(connection.getFunction(eventName)).willReturn(eventFunction);
		
		// when
		scriptRunner.submit(connection, eventName);
		executor.runUntilIdle();
		
		// then
		verify(continuationCoordinator, times(2)).execute(associatedScriptExecutionEnvironment, eventFunction);
		verify(connection).getFunction(eventName);
		verify(associatedScriptExecutionEnvironment, never()).getFunction(eventName);
		
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
		verify(continuationCoordinator).execute(associatedScriptExecutionEnvironment, eventFunction);
	}
	
	@Test
	public void testWebSocketJJMessageEventWithContinuations() {
		
		// given
		givenAWebSocketMessage();
		given(currentScriptContext.scriptExecutionEnvironment()).willReturn(associatedScriptExecutionEnvironment);
		
		String eventName = EventNameHelper.makeEventName("jason", "miller", "rules");
		given(continuationState.type()).willReturn(ContinuationType.JJMessage);
		given(continuationCoordinator.execute(associatedScriptExecutionEnvironment, eventFunction)).willReturn(continuationState);
		
		given(continuationCoordinator.resumeContinuation((String)any(), (ScriptExecutionEnvironment)any(), any()))
			.willReturn(continuationState)
			.willReturn(null);
		
		// when
		scriptRunner.submit(connection, eventName);
		executor.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(associatedScriptExecutionEnvironment, eventFunction);
		
		// given
		given(scriptExecutorFactory.isScriptThread()).willReturn(true);
		
		// when
		scriptRunner.restartAfterContinuation(null, null);
		scriptRunner.restartAfterContinuation(null, null);
		
		// then
		verify(continuationCoordinator, times(2)).resumeContinuation((String)any(), (ScriptExecutionEnvironment)any(), any());
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
		given(moduleScriptExecutionEnvironment.initialized()).willReturn(false);
		given(moduleScriptExecutionEnvironment.baseName()).willReturn(baseName);
		given(currentScriptContext.baseName()).willReturn(baseName);
		given(currentScriptContext.scriptExecutionEnvironment()).willReturn(moduleScriptExecutionEnvironment);
		given(currentScriptContext.moduleScriptExecutionEnvironment()).willReturn(moduleScriptExecutionEnvironment);
		given(scriptExecutionEnvironmentHelper.scriptExecutionEnvironmentFor(baseName, "module")).willReturn(moduleScriptExecutionEnvironment);
		
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
		verify(continuationCoordinator).execute(moduleScriptExecutionEnvironment);
		
		// given
		given(currentScriptContext.type()).willReturn(ScriptContextType.DocumentRequest);
		given(scriptExecutorFactory.isScriptThread()).willReturn(true);
		given(currentScriptContext.documentRequestProcessor()).willReturn(documentRequestProcessor);
		given(documentRequestProcessor.state()).willReturn(DocumentRequestState.Uninitialized);
		
		// when
		executor.runUntilIdle();
		
		// then
		verify(moduleScriptExecutionEnvironment).initialized(true);
		verify(continuationCoordinator).resumeContinuation(module.pendingKey(), moduleScriptExecutionEnvironment, null);
	}
	
	@Test
	public void testModuleScriptWithContinuation() {
		
		// given
		RequiredModule module = givenAModuleRequire();
		given(continuationCoordinator.execute(moduleScriptExecutionEnvironment)).willReturn(continuationState);
		given(continuationState.type()).willReturn(ContinuationType.JJMessage);
		
		// when
		scriptRunner.submit(module);
		executor.runNextPendingCommand();
		
		// then
		verify(continuationCoordinator).execute(moduleScriptExecutionEnvironment);
		
		// given
		given(scriptExecutorFactory.isScriptThread()).willReturn(true);
		given(currentScriptContext.type()).willReturn(ScriptContextType.ModuleInitialization);
		given(continuationCoordinator.execute(moduleScriptExecutionEnvironment)).willReturn(null);
		
		// when
		scriptRunner.restartAfterContinuation("blah", null);
		executor.runNextPendingCommand();
		
		// then
		verify(continuationCoordinator).resumeContinuation("blah", moduleScriptExecutionEnvironment, null);
		
		// given
		given(currentScriptContext.httpRequest()).willReturn(httpRequest);
		given(documentRequestProcessor.state()).willReturn(DocumentRequestState.Uninitialized);
		
		// when
		executor.runNextPendingCommand();
	}

}
