package jj.document;

import static jj.resource.AssetResource.*;
import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.resource.AssetResource;
import jj.resource.ResourceFinder;
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
@Singleton
class ScriptHelperDocumentFilter implements DocumentFilter {

	private final Configuration configuration;
	private final CurrentScriptContext context;
	private final ResourceFinder resourceFinder;

	@Inject
	public ScriptHelperDocumentFilter(
		final Configuration configuration, 
		final CurrentScriptContext context,
		final ResourceFinder resourceFinder
	) {
		this.context = context;
		this.configuration = configuration;
		this.resourceFinder = resourceFinder;
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
			
			// internal version of jquery
			// it's versioned already, so no need for sha-ing
			addScript(documentRequest.document(), "/" + resourceFinder.findResource(AssetResource.class, JQUERY_JS).baseName());
			
			// jj script
			String wsURI = "ws" + 
				(documentRequest.httpRequest().secure() ? "s" : "") + 
				"://" + 
				documentRequest.httpRequest().host() + 
				"/" + 
				scriptBundle.toSocketUri();
			
			Element jjScript = 
				makeScriptTag(documentRequest.document(), resourceFinder.findResource(AssetResource.class, JJ_JS).uri())
				.attr("id", "jj-connector-script")
				.attr("data-jj-socket-url", wsURI)
				.attr(
					"data-jj-startup-messages", 
					context.documentRequestProcessor().startupJJMessages().toString()
				);
			if (configuration.debugClient()) {
				jjScript.attr("data-jj-debug", "true");
			}
			addScript(documentRequest.document(), jjScript);
			
			// associated scripts
			addScript(documentRequest.document(), scriptBundle, scriptBundle.sharedScriptResource());
			addScript(documentRequest.document(), scriptBundle, scriptBundle.clientScriptResource());
		}
	}

	public boolean needsIO(final DocumentRequest documentRequest) {
		return false;
	}
}
