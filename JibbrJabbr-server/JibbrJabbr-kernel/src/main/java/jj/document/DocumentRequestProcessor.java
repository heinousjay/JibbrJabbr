package jj.document;

import java.util.List;

import jj.script.AssociatedScriptBundle;
import jj.servable.RequestProcessor;
import jj.http.HttpRequest;
import jj.jjmessage.JJMessage;

import org.jsoup.nodes.Document;

public interface DocumentRequestProcessor extends RequestProcessor {

	HttpRequest httpRequest();
	
	AssociatedScriptBundle associatedScriptBundle();
	
	DocumentRequestProcessor associatedScriptBundle(AssociatedScriptBundle scriptBundle);

	Document document();
	
	String baseName();

	/**
	 * called by the script executor when it has completed processing this
	 */
	void respond();

	/**
	 * @return 
	 * 
	 */
	DocumentRequestProcessor startingInitialExecution();

	/**
	 * @return
	 */
	DocumentRequestProcessor startingReadyFunction();

	/**
	 * @return
	 */
	DocumentRequestState state();



	/**
	 * adds a message intended to be processed a framework startup
	 * on the client.  initially intended for event bindings but
	 * some other case may come up
	 * @param message
	 * @return this, for chaining
	 */
	DocumentRequestProcessor addStartupJJMessage(JJMessage message);

	/**
	 * @return
	 */
	List<JJMessage> startupJJMessages();

	/**
	 * @return
	 */
	String uri();

}