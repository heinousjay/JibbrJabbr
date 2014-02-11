package jj.script;

import static org.mockito.BDDMockito.*;
import jj.execution.MockJJExecutor;
import jj.http.HttpRequest;
import jj.http.server.WebSocketConnection;
import jj.http.server.servable.document.DocumentRequestProcessor;
import jj.http.server.servable.document.DocumentRequestState;
import jj.resource.document.CurrentDocumentRequestProcessor;
import jj.resource.document.DocumentScriptEnvironment;
import jj.resource.script.ModuleScriptEnvironment;
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
	
	private ContinuationPendingKey pendingKey;
	
	private String baseName = "index";
	
	private Document document;
	
	private @Mock ScriptableObject scriptable;
	
	private @Mock DocumentScriptEnvironment documentScriptEnvironment;
	
	private @Mock ModuleScriptEnvironment moduleScriptEnvironment;
		
	private @Mock ScriptResource scriptResource;
	
	private @Mock ContinuationCoordinatorImpl continuationCoordinator;
	
	private @Mock CurrentScriptContext currentScriptContext;
	
	private MockJJExecutor executors;
	
	private @Mock HttpRequest httpRequest;
	
	private ScriptRunnerImpl scriptRunner;
	
	private @Mock DocumentRequestProcessor documentRequestProcessor;
	
	private @Mock WebSocketConnection connection;
	
	private @Mock Callable eventFunction;
	
	private ScriptContext httpRequestContext;
	
	@Before
	public void before() {
		
		pendingKey = new ContinuationPendingKey();
		
		given(currentScriptContext.webSocketConnectionHost()).willReturn(documentScriptEnvironment);
		
		executors = new MockJJExecutor();
		
		scriptRunner = new ScriptRunnerImpl(
			continuationCoordinator,
			currentScriptContext,
			new CurrentDocumentRequestProcessor(),
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

		given(currentScriptContext.documentRequestProcessor()).willReturn(documentRequestProcessor);
		given(currentScriptContext.type()).willReturn(ScriptContextType.DocumentRequest);
	}
	
	@Test
	public void testInitialDocumentRequestWithNoContinuations() throws Exception {
		
		// given
		givenADocumentRequest();
		given(continuationCoordinator.execute(documentScriptEnvironment)).willReturn(null);
		given(continuationCoordinator.execute(documentScriptEnvironment, eventFunction)).willReturn(null);
		
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
		given(continuationCoordinator.execute(documentScriptEnvironment)).willReturn(null);
		givenADocumentRequest();
		
		given(documentRequestProcessor.state()).willReturn(DocumentRequestState.InitialExecution);
		
		// when
		scriptRunner.submit(documentRequestProcessor);
		executors.runUntilIdle();
		
		// then
		verify(documentRequestProcessor).startingInitialExecution();
		
		// given
		executors.isScriptThread = true;
		given(continuationCoordinator.resumeContinuation(documentScriptEnvironment, pendingKey, null)).willReturn(pendingKey);
		given(continuationCoordinator.execute(documentScriptEnvironment, eventFunction)).willReturn(pendingKey);
		given(documentScriptEnvironment.initialized()).willReturn(true);
		
		// when
		scriptRunner.submit("", httpRequestContext, pendingKey, null);
		executors.runUntilIdle();
		
		// then
		verify(documentRequestProcessor).startingReadyFunction();
		verify(documentRequestProcessor).respond(); // verifies execution processing
	}
	
	@Test
	public void testInitialDocumentRequestWithRESTContinuationDuringReadyFunction() throws Exception {
		
		// given
		given(currentScriptContext.scriptEnvironment()).willReturn(documentScriptEnvironment);
		given(continuationCoordinator.execute(documentScriptEnvironment)).willReturn(null);
		given(continuationCoordinator.execute(documentScriptEnvironment, eventFunction)).willReturn(pendingKey);
		
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
		given(continuationCoordinator.resumeContinuation(documentScriptEnvironment, pendingKey, null)).willReturn(null);
		
		// when
		//try (Closer closer = currentDocument.enterScope(documentRequestProcessor)) {
			scriptRunner.submit("", httpRequestContext, pendingKey, null);
			executors.runUntilIdle();
		//}
		
		// then
		verify(documentRequestProcessor).respond(); // verifies execution processing
	}
	
	private void givenAWebSocketMessage() {

		given(connection.baseName()).willReturn(baseName);
		given(connection.getFunction(any(String.class))).willReturn(eventFunction);
		given(connection.webSocketConnectionHost()).willReturn(documentScriptEnvironment);
		given(currentScriptContext.type()).willReturn(ScriptContextType.WebSocket);
		
	}
	
	@Test
	public void testContinuationResumption() throws Exception {
		
		// given
		givenAWebSocketMessage();
		given(currentScriptContext.scriptEnvironment()).willReturn(documentScriptEnvironment);
		executors.isScriptThread = true;
		
		// when
		scriptRunner.submit("", httpRequestContext, pendingKey, null);
		scriptRunner.submit("", httpRequestContext, pendingKey, null);
		executors.runUntilIdle();
		
		// then
		verify(continuationCoordinator, times(2)).resumeContinuation(any(ScriptEnvironment.class), eq(pendingKey), any());
	}
	
	@Test
	public void testWebSocketJJMessageResultWithNoContinuation() throws Exception {
		
		// given
		final String value = "value";
		givenAWebSocketMessage();
		
		// when
		scriptRunner.submitPendingResult(connection, pendingKey, value);
		executors.runUntilIdle();
		
		// then
		verify(continuationCoordinator).resumeContinuation(null, pendingKey, value);
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
		given(continuationCoordinator.execute(moduleScriptEnvironment)).willReturn(null);
		
		// when
		scriptRunner.submit(module, moduleScriptEnvironment);
		executors.runFirstTask();
		
		// given
		given(moduleScriptEnvironment.exports()).willReturn(scriptable);
		given(currentScriptContext.type()).willReturn(ScriptContextType.DocumentRequest);
		executors.isScriptThread = true;
		given(currentScriptContext.documentRequestProcessor()).willReturn(documentRequestProcessor);
		given(documentRequestProcessor.state()).willReturn(DocumentRequestState.Uninitialized);
		module.pendingKey(pendingKey);
		
		// when
		executors.runUntilIdle();
		
		// then
		verify(moduleScriptEnvironment).initialized(true);
		verify(continuationCoordinator).resumeContinuation(moduleScriptEnvironment, module.pendingKey(), scriptable);
	}
	
	@Test
	public void testModuleScriptWithContinuation() throws Exception {
		
		// given
		RequiredModule module = givenAModuleRequire();
		given(continuationCoordinator.execute(moduleScriptEnvironment)).willReturn(pendingKey);
		
		// when
		scriptRunner.submit(module, moduleScriptEnvironment);
		executors.runFirstTask();
		
		// given
		executors.isScriptThread = true;
		module.pendingKey(pendingKey);
		given(currentScriptContext.type()).willReturn(ScriptContextType.ModuleInitialization);
		given(continuationCoordinator.resumeContinuation(moduleScriptEnvironment, module.pendingKey(), scriptable)).willReturn(null);
		
		// when
		scriptRunner.submit("", httpRequestContext, module.pendingKey(), scriptable);
		executors.runFirstTask();
		
		// then
		verify(moduleScriptEnvironment).initialized(true);
		verify(continuationCoordinator).resumeContinuation(moduleScriptEnvironment, module.pendingKey(), scriptable);
	}

}
