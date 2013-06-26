package jj.webbit;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jj.DateFormatHelper;
import jj.jqmessage.JQueryMessage;
import jj.script.AssociatedScriptBundle;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.webbitserver.HttpRequest;
import org.webbitserver.wrapper.HttpRequestWrapper;

public class JJHttpRequest extends HttpRequestWrapper {
	
	public enum State {
		Uninitialized,
		InitialExecution {
			@Override
			public String toString() {
				return "Initial execution";
			}
		},
		ReadyFunctionExecution {
			@Override
			public String toString() {
				return "Ready function execution";
			}
		};
	}
	
	private static final String HEADER_X_HOST = "x-host";
	
	private static final String HEADER_X_SECURE = "x-secure";
	
	private static final String PROCESSING_STATE = "processing state";
	
	private static final String ASSOCIATED_SCRIPT_BUNDLE = "associated script bundle";
	
	private static final String START_TIME = "start time";
	
	private static final String STARTUP_JQUERY_MESSAGES = "startup jquery messages";

	JJHttpRequest(final HttpRequest request) {
		super(request);
		data(START_TIME, System.nanoTime());
		data(PROCESSING_STATE, State.Uninitialized);
	}
	
	public BigDecimal wallTime() {
		long startTime = (long)data(START_TIME);
		return BigDecimal.valueOf(System.nanoTime() - startTime, 6);
	}
	
	public AssociatedScriptBundle associatedScriptBundle() {
		return (AssociatedScriptBundle)data(ASSOCIATED_SCRIPT_BUNDLE);
	}
	
	public JJHttpRequest associatedScriptBundle(AssociatedScriptBundle associatedScriptBundle) {
		data(ASSOCIATED_SCRIPT_BUNDLE, associatedScriptBundle);
		return this;
	}
	
	public JJHttpRequest startingInitialExecution() {
		data(PROCESSING_STATE, State.InitialExecution);
		associatedScriptBundle().initializing(true);
		return this;
	}
	
	public JJHttpRequest startingReadyFunction() {
		data(PROCESSING_STATE, State.ReadyFunctionExecution);
		return this;
	}
	
	public State state() {
		return (State)data(PROCESSING_STATE);
	}
	
	public String host() {
		String xHost = header(HEADER_X_HOST);
		String host = header(HttpHeaders.Names.HOST);
		return xHost == null ? host : xHost;
	}
	
	public boolean secure() {
		return "true".equals(header(HEADER_X_SECURE));
	}
	
	public URI absoluteUri() {
		return URI.create(
			new StringBuilder("http")
				.append(secure() ? "s" : "")
				.append("://")
				.append(host())
				.append(uri())
				.toString()
		);
	}

	/**
	 * adds a message intended to be processed a framework startup
	 * on the client.  initially intended for event bindings but
	 * some other case may come up
	 * @param message
	 */
	public JJHttpRequest addStartupJQueryMessage(final JQueryMessage message) {
		@SuppressWarnings("unchecked")
		ArrayList<JQueryMessage> messages = (ArrayList<JQueryMessage>)data().get(STARTUP_JQUERY_MESSAGES);
		if (messages == null) {
			messages = new ArrayList<>();
			data().put(STARTUP_JQUERY_MESSAGES, messages);
		}
		messages.add(message);
		return this;
	}
	
	public List<JQueryMessage> startupJQueryMessages() {
		@SuppressWarnings("unchecked")
		ArrayList<JQueryMessage> messages = (ArrayList<JQueryMessage>)data().remove(STARTUP_JQUERY_MESSAGES);
		return messages == null ? Collections.<JQueryMessage>emptyList() : messages;
	}
	
	@Override
	public String toString() {
		return "httpRequest[" +
			remoteAddress() +
			"] started at " +
			DateFormatHelper.basicFormat(timestamp()) +
			" with data " +
			data();
	}
}
