package jj.request;

import jj.script.ScriptBundle;
import jj.webbit.JJHttpRequest;

import org.jsoup.nodes.Document;

public interface DocumentRequestProcessor extends RequestProcessor {

	JJHttpRequest httpRequest();
	
	ScriptBundle scriptBundle();
	void scriptBundle(ScriptBundle scriptBundle);

	Document document();
	
	String baseName();

	/**
	 * called by the script executor when it has completed processing this
	 */
	void respond();

}