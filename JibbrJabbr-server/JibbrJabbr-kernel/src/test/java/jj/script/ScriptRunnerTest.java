package jj.script;

import static org.mockito.BDDMockito.*;

import jj.engine.HostEvent;
import jj.execution.MockJJExecutor;
import jj.http.HttpRequest;
import jj.http.server.JJWebSocketConnection;
import jj.http.server.servable.document.DocumentRequestProcessor;
import jj.http.server.servable.document.DocumentRequestState;
import jj.resource.document.DocumentScriptEnvironment;
import jj.resource.script.ModuleScriptEnvironment;
import jj.resource.script.ScriptEnvironment;
import jj.resource.script.ScriptResource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.ScriptableObject;

@RunWith(MockitoJUnitRunner.class)
public class ScriptRunnerTest {
	
	String baseName = "index";
	
	Document document;
	
	@Mock ScriptableObject scriptable;
	
	@Mock DocumentScriptEnvironment documentScriptEnvironment;
	
	@Mock ModuleScriptEnvironment moduleScriptEnvironment;
		
	@Mock ScriptResource scriptResource;
	
	@Mock ContinuationCoordinator continuationCoordinator;
	
	@Mock CurrentScriptContext currentScriptContext;
	
	MockJJExecutor executors;
	
	@Mock HttpRequest httpRequest;
	
	ScriptRunnerImpl scriptRunner;
	
	@Mock DocumentRequestProcessor documentRequestProcessor;
	
	@Mock JJWebSocketConnection connection;
	
	@Mock Callable eventFunction;
	
	ScriptContext httpRequestContext;
	
	@Before
	public void before() {
		
		given(currentScriptContext.documentScriptEnvironment()).willReturn(documentScriptEnvironment);
		
		executors = new MockJJExecutor();
		
		scriptRunner = new ScriptRunnerImpl(
			continuationCoordinator,
			currentScriptContext,
			executors
		);
		
		document = Jsoup.parse("<html><head><title>what</title></head><body></body></html>");
		
		given(documentRequestProcessor.baseName()).willReturn(baseName);
		given(documentRequestProcessor.document()).willReturn(document);
		given(documentRequestProcessor.documentScriptEnvironment()).willReturn(documentScriptEnvironment);
		
		
		given(documentScriptEnvironment.getFunction(ScriptRunner.READY_FUNCTION_KEY)).willReturn(eventFunction);
		
		httpRequestContext = new ScriptContext(null, documentRequestProcessor);
	}
	
	private void givenADocumentRequest() {

		given(currentScriptContext.httpRequest()).willReturn(httpRequest);
		given(currentScriptContext.documentRequestProcessor()).willReturn(documentRequestProcessor);
		given(currentScriptContext.type()).willReturn(ScriptContextType.DocumentRequest);
	}
	
	@Test
	public void testInitialDocumentRequestWithNoContinuations() throws Exception {
		
		// given
		givenADocumentRequest();
		given(continuationCoordinator.execute(documentScriptEnvironment)).willReturn(true);
		given(continuationCoordinator.execute(documentScriptEnvironment, eventFunction)).willReturn(true);
		
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
		given(currentScriptContext.scriptEnvironment()).willReturn(documentScriptEnvironment);
		given(continuationCoordinator.execute(documentScriptEnvironment)).willReturn(false);
		givenADocumentRequest();
		
		given(documentRequestProcessor.state()).willReturn(DocumentRequestState.InitialExecution);
		
		// when
		scriptRunner.submit(documentRequestProcessor);
		executors.runUntilIdle();
		
		// then
		verify(documentRequestProcessor).startingInitialExecution();
		
		// given
		executors.isScriptThread = true;
		given(continuationCoordinator.resumeContinuation("", documentScriptEnvironment, null)).willReturn(true);
		given(continuationCoordinator.execute(documentScriptEnvironment, eventFunction)).willReturn(true);
		
		// when
		scriptRunner.submit("", httpRequestContext, "", null);
		executors.runUntilIdle();
		
		// then
		verify(documentRequestProcessor).startingReadyFunction();
		verify(documentRequestProcessor).respond(); // verifies execution processing
	}
	
	@Test
	public void testInitialDocumentRequestWithRESTContinuationDuringReadyFunction() throws Exception {
		
		// given
		given(currentScriptContext.scriptEnvironment()).willReturn(documentScriptEnvironment);
		given(continuationCoordinator.execute(documentScriptEnvironment)).willReturn(true);
		given(continuationCoordinator.execute(documentScriptEnvironment, eventFunction)).willReturn(false);
		
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
		given(continuationCoordinator.resumeContinuation("", documentScriptEnvironment, null)).willReturn(true);
		
		// when
		scriptRunner.submit("", httpRequestContext, "", null);
		executors.runUntilIdle();
		
		// then
		verify(documentRequestProcessor).respond(); // verifies execution processing
	}
	
	private void givenAWebSocketMessage() {

		given(connection.baseName()).willReturn(baseName);
		given(connection.getFunction(any(String.class))).willReturn(eventFunction);
		given(connection.documentScriptEnvironment()).willReturn(documentScriptEnvironment);
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
		verify(continuationCoordinator).execute(documentScriptEnvironment, eventFunction, connection);
	}
	
	@Test
	public void testWebSocketHostEventWithContinuations() throws Exception {
		
		// given
		givenAWebSocketMessage();
		given(currentScriptContext.scriptEnvironment()).willReturn(documentScriptEnvironment);
		given(continuationCoordinator.execute(documentScriptEnvironment, eventFunction, connection)).willReturn(false);
		given(continuationCoordinator.resumeContinuation(anyString(), any(ScriptEnvironment.class), any()))
			.willReturn(false)
			.willReturn(true);
		
		// when
		scriptRunner.submit(connection, HostEvent.clientConnected, connection);
		executors.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(documentScriptEnvironment, eventFunction, connection);
		
		// given
		executors.isScriptThread = true;
		
		// when
		scriptRunner.submit("", httpRequestContext, "", null);
		scriptRunner.submit("", httpRequestContext, "", null);
		executors.runUntilIdle();
		
		// then
		verify(continuationCoordinator, times(2)).resumeContinuation(anyString(), any(ScriptEnvironment.class), any());
	}
	
	@Test
	public void testWebSocketEventDelegationToGlobal() throws Exception {
		
		// given 
		givenAWebSocketMessage();
		String eventName = EventNameHelper.makeEventName("jason", "miller", "rules");
		given(documentScriptEnvironment.getFunction(eventName)).willReturn(eventFunction);
		given(connection.getFunction(eventName)).willReturn(null);
		
		// when
		scriptRunner.submit(connection, eventName);
		executors.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(documentScriptEnvironment, eventFunction);
		verify(connection).getFunction(eventName);
		verify(documentScriptEnvironment).getFunction(eventName);

		//given
		eventName = EventNameHelper.makeEventName("jason", "miller", "rocks");
		given(documentScriptEnvironment.getFunction(eventName)).willReturn(null);
		given(connection.getFunction(eventName)).willReturn(eventFunction);
		
		// when
		scriptRunner.submit(connection, eventName);
		executors.runUntilIdle();
		
		// then
		verify(continuationCoordinator, times(2)).execute(documentScriptEnvironment, eventFunction);
		verify(connection).getFunction(eventName);
		verify(documentScriptEnvironment, never()).getFunction(eventName);
		
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
		verify(continuationCoordinator).execute(documentScriptEnvironment, eventFunction);
	}
	
	@Test
	public void testWebSocketJJMessageEventWithContinuations() throws Exception {
		
		// given
		givenAWebSocketMessage();
		given(currentScriptContext.scriptEnvironment()).willReturn(documentScriptEnvironment);
		
		String eventName = EventNameHelper.makeEventName("jason", "miller", "rules");
		given(continuationCoordinator.execute(documentScriptEnvironment, eventFunction)).willReturn(false);
		
		given(continuationCoordinator.resumeContinuation(anyString(), any(ScriptEnvironment.class), any()))
			.willReturn(false)
			.willReturn(true);
		
		// when
		scriptRunner.submit(connection, eventName);
		executors.runUntilIdle();
		
		// then
		verify(continuationCoordinator).execute(documentScriptEnvironment, eventFunction);
		
		// given
		executors.isScriptThread = true;
		
		// when
		scriptRunner.submit("", httpRequestContext, "", null);
		scriptRunner.submit("", httpRequestContext, "", null);
		executors.runUntilIdle();
		
		// then
		verify(continuationCoordinator, times(2)).resumeContinuation(anyString(), any(ScriptEnvironment.class), any());
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
		given(moduleScriptEnvironment.initialized()).willReturn(false);
		given(moduleScriptEnvironment.baseName()).willReturn(baseName);
		given(currentScriptContext.baseName()).willReturn(baseName);
		given(currentScriptContext.scriptEnvironment()).willReturn(moduleScriptEnvironment);
		given(currentScriptContext.moduleScriptEnvironment()).willReturn(moduleScriptEnvironment);
		
		RequiredModule requiredModule = new RequiredModule("module", currentScriptContext);
		given(currentScriptContext.requiredModule()).willReturn(requiredModule);
		return requiredModule;
	}
	
	@Test
	public void testModuleScriptWithNoContinuation() throws Exception {
		
		// given
		RequiredModule module = givenAModuleRequire();
		given(continuationCoordinator.execute(moduleScriptEnvironment)).willReturn(true);
		
		// when
		scriptRunner.submit(module, moduleScriptEnvironment);
		executors.runFirstTask();
		
		// given
		given(moduleScriptEnvironment.exports()).willReturn(scriptable);
		given(currentScriptContext.type()).willReturn(ScriptContextType.DocumentRequest);
		executors.isScriptThread = true;
		given(currentScriptContext.documentRequestProcessor()).willReturn(documentRequestProcessor);
		given(documentRequestProcessor.state()).willReturn(DocumentRequestState.Uninitialized);
		
		// when
		executors.runUntilIdle();
		
		// then
		verify(moduleScriptEnvironment).initialized(true);
		verify(continuationCoordinator).resumeContinuation(module.pendingKey(), moduleScriptEnvironment, scriptable);
	}
	
	@Test
	public void testModuleScriptWithContinuation() throws Exception {
		
		// given
		RequiredModule module = givenAModuleRequire();
		given(continuationCoordinator.execute(moduleScriptEnvironment)).willReturn(false);
		
		// when
		scriptRunner.submit(module, moduleScriptEnvironment);
		executors.runFirstTask();
		
		// given
		executors.isScriptThread = true;
		given(currentScriptContext.type()).willReturn(ScriptContextType.ModuleInitialization);
		given(continuationCoordinator.resumeContinuation(module.pendingKey(), moduleScriptEnvironment, scriptable)).willReturn(true);
		
		// when
		scriptRunner.submit("", httpRequestContext, module.pendingKey(), scriptable);
		executors.runFirstTask();
		
		// then
		verify(moduleScriptEnvironment).initialized(true);
		verify(continuationCoordinator).resumeContinuation(module.pendingKey(), moduleScriptEnvironment, scriptable);
	}

}
