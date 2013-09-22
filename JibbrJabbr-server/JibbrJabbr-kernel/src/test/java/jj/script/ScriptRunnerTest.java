package jj.script;

import static org.mockito.BDDMockito.*;

import jj.engine.HostEvent;
import jj.execution.MockJJExecutors;
import jj.http.HttpRequest;
import jj.http.server.JJWebSocketConnection;
import jj.http.server.servable.document.DocumentRequestProcessor;
import jj.http.server.servable.document.DocumentRequestState;
import jj.resource.document.ScriptResource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Scriptable;

@RunWith(MockitoJUnitRunner.class)
public class ScriptRunnerTest {
	
	// it takes a village to isolate the ScriptRunner.  it
	// sits on the top of a serious mountain of man-hours.
	
	String baseName = "index";
	
	Document document;
	
	@Mock Scriptable scriptable;
	
	@Mock DocumentScriptExecutionEnvironment associatedScriptExecutionEnvironment;
	
	@Mock ModuleScriptExecutionEnvironment moduleScriptExecutionEnvironment;
		
	@Mock ScriptResource scriptResource;
	
	@Mock ScriptExecutionEnvironmentHelper scriptExecutionEnvironmentHelper;
	
	@Mock ContinuationCoordinator continuationCoordinator;
	
	@Mock CurrentScriptContext currentScriptContext;
	
	MockJJExecutors executors;
	
	@Mock HttpRequest httpRequest;
	
	ScriptRunnerImpl scriptRunner;
	
	@Mock DocumentRequestProcessor documentRequestProcessor;
	
	@Mock JJWebSocketConnection connection;
	
	@Mock Callable eventFunction;
	
	ScriptContext httpRequestContext;
	
	@Before
	public void before() {
		
		when(scriptExecutionEnvironmentHelper.scriptExecutionEnvironmentFor(baseName)).thenReturn(associatedScriptExecutionEnvironment);
		
		when(currentScriptContext.documentScriptExecutionEnvironment()).thenReturn(associatedScriptExecutionEnvironment);
		
		executors = new MockJJExecutors();
		
		scriptRunner = new ScriptRunnerImpl(
			scriptExecutionEnvironmentHelper,
			continuationCoordinator,
			currentScriptContext,
			executors
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
	public void testDocumentWithNoScript() throws Exception {
		
		// given
		given(scriptExecutionEnvironmentHelper.scriptExecutionEnvironmentFor(baseName)).willReturn(null);
		givenADocumentRequest();
		
		// when
		scriptRunner.submit(documentRequestProcessor);
		executors.runUntilIdle();
		
		// then
		verify(documentRequestProcessor).respond();
	}
	
	@Test
	public void testInitialDocumentRequestWithNoContinuations() throws Exception {
		
		// given
		givenADocumentRequest();
		given(continuationCoordinator.execute(associatedScriptExecutionEnvironment)).willReturn(true);
		given(continuationCoordinator.execute(associatedScriptExecutionEnvironment, eventFunction)).willReturn(true);
		
		// when
		scriptRunner.submit(documentRequestProcessor);
		executors.runUntilIdle();
		
		// then
		verify(documentRequestProcessor).startingInitialExecution();
		verify(documentRequestProcessor).startingReadyFunction();
		verify(documentRequestProcessor).respond();
	}
	
	@Test
	public void testInitialDocumentRequestWithRESTContinuationDuringInitialization() throws Exception {
		
		// given
		given(currentScriptContext.scriptExecutionEnvironment()).willReturn(associatedScriptExecutionEnvironment);
		given(continuationCoordinator.execute(associatedScriptExecutionEnvironment)).willReturn(false);
		givenADocumentRequest();
		
		given(documentRequestProcessor.state()).willReturn(DocumentRequestState.InitialExecution);
		
		// when
		scriptRunner.submit(documentRequestProcessor);
		executors.runUntilIdle();
		
		// then
		verify(documentRequestProcessor).startingInitialExecution();
		
		// given
		executors.isScriptThread = true;
		given(continuationCoordinator.resumeContinuation("", associatedScriptExecutionEnvironment, null)).willReturn(true);
		given(continuationCoordinator.execute(associatedScriptExecutionEnvironment, eventFunction)).willReturn(true);
		
		// when
		scriptRunner.restartAfterContinuation("", null);
		
		// then
		verify(documentRequestProcessor).startingReadyFunction();
		verify(documentRequestProcessor).respond(); // verifies execution processing
	}
	
	@Test
	public void testInitialDocumentRequestWithRESTContinuationDuringReadyFunction() throws Exception {
		
		// given
		given(currentScriptContext.scriptExecutionEnvironment()).willReturn(associatedScriptExecutionEnvironment);
		given(continuationCoordinator.execute(associatedScriptExecutionEnvironment)).willReturn(true);
		given(continuationCoordinator.execute(associatedScriptExecutionEnvironment, eventFunction)).willReturn(false);
		
		given(documentRequestProcessor.state()).willReturn(DocumentRequestState.ReadyFunctionExecution);
		givenADocumentRequest();
		
		// when
		scriptRunner.submit(documentRequestProcessor);
		executors.runUntilIdle();
		
		// then
		verify(documentRequestProcessor).startingInitialExecution();
		verify(documentRequestProcessor).startingReadyFunction();
		
		// given
		executors.isScriptThread = true;
		given(continuationCoordinator.resumeContinuation("", associatedScriptExecutionEnvironment, null)).willReturn(true);
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
	public void testWebSocketHostEventWithNoContinuation() throws Exception {
		
		// given
		givenAWebSocketMessage();
		
		// when
		scriptRunner.submit(connection, HostEvent.clientConnected, connection);
		executors.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(associatedScriptExecutionEnvironment, eventFunction, connection);
	}
	
	@Test
	public void testWebSocketHostEventWithContinuations() throws Exception {
		
		// given
		givenAWebSocketMessage();
		given(currentScriptContext.scriptExecutionEnvironment()).willReturn(associatedScriptExecutionEnvironment);
		given(continuationCoordinator.execute(associatedScriptExecutionEnvironment, eventFunction, connection)).willReturn(false);
		given(continuationCoordinator.resumeContinuation((String)any(), (ScriptExecutionEnvironment)any(), any()))
			.willReturn(false)
			.willReturn(true);
		
		// when
		scriptRunner.submit(connection, HostEvent.clientConnected, connection);
		executors.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(associatedScriptExecutionEnvironment, eventFunction, connection);
		
		// given
		executors.isScriptThread = true;
		
		// when
		scriptRunner.restartAfterContinuation(null, null);
		scriptRunner.restartAfterContinuation(null, null);
		
		// then
		verify(continuationCoordinator, times(2)).resumeContinuation((String)any(), (ScriptExecutionEnvironment)any(), any());
	}
	
	@Test
	public void testWebSocketEventDelegationToGlobal() throws Exception {
		
		// given 
		givenAWebSocketMessage();
		String eventName = EventNameHelper.makeEventName("jason", "miller", "rules");
		given(associatedScriptExecutionEnvironment.getFunction(eventName)).willReturn(eventFunction);
		given(connection.getFunction(eventName)).willReturn(null);
		
		// when
		scriptRunner.submit(connection, eventName);
		executors.runUntilIdle();
		
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
		executors.runUntilIdle();
		
		// then
		verify(continuationCoordinator, times(2)).execute(associatedScriptExecutionEnvironment, eventFunction);
		verify(connection).getFunction(eventName);
		verify(associatedScriptExecutionEnvironment, never()).getFunction(eventName);
		
	}
	
	@Test
	public void testWebSocketJJMessageEventWithNoContinuation() throws Exception {
		
		// given
		givenAWebSocketMessage();
		String eventName = EventNameHelper.makeEventName("jason", "miller", "rules");
		
		// when
		scriptRunner.submit(connection, eventName);
		executors.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(associatedScriptExecutionEnvironment, eventFunction);
	}
	
	@Test
	public void testWebSocketJJMessageEventWithContinuations() throws Exception {
		
		// given
		givenAWebSocketMessage();
		given(currentScriptContext.scriptExecutionEnvironment()).willReturn(associatedScriptExecutionEnvironment);
		
		String eventName = EventNameHelper.makeEventName("jason", "miller", "rules");
		given(continuationCoordinator.execute(associatedScriptExecutionEnvironment, eventFunction)).willReturn(false);
		
		given(continuationCoordinator.resumeContinuation((String)any(), (ScriptExecutionEnvironment)any(), any()))
			.willReturn(false)
			.willReturn(true);
		
		// when
		scriptRunner.submit(connection, eventName);
		executors.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(associatedScriptExecutionEnvironment, eventFunction);
		
		// given
		executors.isScriptThread = true;
		
		// when
		scriptRunner.restartAfterContinuation(null, null);
		scriptRunner.restartAfterContinuation(null, null);
		
		// then
		verify(continuationCoordinator, times(2)).resumeContinuation((String)any(), (ScriptExecutionEnvironment)any(), any());
	}
	
	@Test
	public void testWebSocketJJMessageResultWithNoContinuation() throws Exception {
		
		// given
		final String key = "0";
		final String value = "value";
		givenAWebSocketMessage();
		
		// when
		scriptRunner.submitPendingResult(connection, key, value);
		executors.runUntilIdle();
		
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
	public void testModuleScriptWithNoContinuation() throws Exception {
		
		// given
		RequiredModule module = givenAModuleRequire();
		given(continuationCoordinator.execute(moduleScriptExecutionEnvironment)).willReturn(true);
		
		// when
		scriptRunner.submit(module);
		executors.runFirstTask();
		
		// given
		given(moduleScriptExecutionEnvironment.exports()).willReturn(scriptable);
		given(currentScriptContext.type()).willReturn(ScriptContextType.DocumentRequest);
		executors.isScriptThread = true;
		given(currentScriptContext.documentRequestProcessor()).willReturn(documentRequestProcessor);
		given(documentRequestProcessor.state()).willReturn(DocumentRequestState.Uninitialized);
		
		// when
		executors.runUntilIdle();
		
		// then
		verify(moduleScriptExecutionEnvironment).initialized(true);
		verify(continuationCoordinator).resumeContinuation(module.pendingKey(), moduleScriptExecutionEnvironment, scriptable);
	}
	
	@Test
	public void testModuleScriptWithContinuation() throws Exception {
		
		// given
		RequiredModule module = givenAModuleRequire();
		given(continuationCoordinator.execute(moduleScriptExecutionEnvironment)).willReturn(false);
		
		// when
		scriptRunner.submit(module);
		executors.runFirstTask();
		
		// given
		executors.isScriptThread = true;
		given(currentScriptContext.type()).willReturn(ScriptContextType.ModuleInitialization);
		given(continuationCoordinator.resumeContinuation(module.pendingKey(), moduleScriptExecutionEnvironment, scriptable)).willReturn(true);
		
		// when
		scriptRunner.restartAfterContinuation(module.pendingKey(), scriptable);
		executors.runFirstTask();
		
		// then
		verify(moduleScriptExecutionEnvironment).initialized(true);
		verify(continuationCoordinator).resumeContinuation(module.pendingKey(), moduleScriptExecutionEnvironment, scriptable);
	}

}
