package jj.document;

import jj.Configuration;
import jj.resource.ScriptResource;
import jj.script.CurrentScriptContext;
import jj.script.AssociatedScriptBundle;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Add our standard dependencies (jQuery, socket-connect) and any automatic
 * client scripts to the outgoing document
 * 
 * @author jason
 * 
 */
public class ScriptHelperDocumentFilter implements DocumentFilter {

	public static final String JQUERY_URI = "/jquery-1.8.3.min.js";
	public static final String SOCKET_CONNECT_URI = "/socket-connect.js";

	private final Configuration configuration;
	private final CurrentScriptContext context;

	public ScriptHelperDocumentFilter(final Configuration configuration, final CurrentScriptContext context) {
		this.context = context;
		this.configuration = configuration;
	}

	private void addScript(Document document, AssociatedScriptBundle bundle, ScriptResource scriptResource) {
		if (scriptResource != null) {
			addScript(document, "/" + bundle.toUri() + scriptResource.type().suffix());
		}
	}

	private Element makeScriptTag(Document document, String uri) {
		return document
			.createElement("script")
			.attr("type", "text/javascript")
			.attr("src", uri);
	}

	private void addScript(Document document, Element scriptTag) {
		document.select("head").append(scriptTag.toString());
	}

	private void addScript(Document document, String uri) {
		addScript(document, makeScriptTag(document, uri));
	}

	@Override
	public void filter(final DocumentRequest documentRequest) {
		AssociatedScriptBundle scriptBundle = context.associatedScriptBundle();
		if (scriptBundle != null) {
			addScript(documentRequest.document(), JQUERY_URI);
			
			String wsURI = "ws://" + documentRequest.httpRequest().host() + "/" + scriptBundle.toSocketUri();
			
			Element socketConnect = 
				makeScriptTag(documentRequest.document(), SOCKET_CONNECT_URI)
				.attr("id", "jj-connector-script")
				.attr("data-jj-socket-url", wsURI)
				.attr(
					"data-jj-startup-messages", 
					context.httpRequest().startupJQueryMessages().toString()
				);
			if (configuration.debugClient()) {
				socketConnect.attr("data-jj-debug", "true");
			}
			addScript(documentRequest.document(), socketConnect);
			
			if (scriptBundle.clientScriptResource() != null) {
	
			}
	
			addScript(documentRequest.document(), scriptBundle, scriptBundle.sharedScriptResource());
			addScript(documentRequest.document(), scriptBundle, scriptBundle.clientScriptResource());
		}
	}

	public boolean needsIO(final DocumentRequest documentRequest) {
		return false;
	}
}
