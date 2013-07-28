package jj.document;

import jj.script.AssociatedScriptBundle;
import jj.http.HttpRequest;
import jj.http.RequestProcessor;

import org.jsoup.nodes.Document;

public interface DocumentRequestProcessor extends RequestProcessor {

	HttpRequest httpRequest();
	
	AssociatedScriptBundle associatedScriptBundle();
	
	void scriptBundle(AssociatedScriptBundle scriptBundle);

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

}