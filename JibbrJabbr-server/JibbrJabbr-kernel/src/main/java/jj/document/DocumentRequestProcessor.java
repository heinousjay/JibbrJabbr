package jj.document;

import jj.script.AssociatedScriptBundle;
import jj.webbit.JJHttpRequest;
import jj.webbit.RequestProcessor;

import org.jsoup.nodes.Document;

public interface DocumentRequestProcessor extends RequestProcessor {

	JJHttpRequest httpRequest();
	
	AssociatedScriptBundle associatedScriptBundle();
	
	void scriptBundle(AssociatedScriptBundle scriptBundle);

	Document document();
	
	String baseName();

	/**
	 * called by the script executor when it has completed processing this
	 */
	void respond();

}