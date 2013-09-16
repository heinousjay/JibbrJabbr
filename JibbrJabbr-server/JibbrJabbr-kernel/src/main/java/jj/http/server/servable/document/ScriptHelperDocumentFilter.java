package jj.http.server.servable.document;

import static jj.resource.asset.AssetResource.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.resource.ResourceFinder;
import jj.resource.asset.AssetResource;
import jj.resource.script.ScriptResourceType;
import jj.script.CurrentScriptContext;
import jj.script.DocumentScriptExecutionEnvironment;

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
	public void filter(final DocumentRequestProcessor documentRequestProcessor) {
		DocumentScriptExecutionEnvironment scriptExecutionEnvironment = context.documentScriptExecutionEnvironment();
		if (scriptExecutionEnvironment != null) {
			
			// internal version of jquery
			// it's versioned already, so no need for sha-ing
			addScript(documentRequestProcessor.document(), "/" + resourceFinder.findResource(AssetResource.class, JQUERY_JS).baseName());
			
			// jj script
			String wsURI = "ws" + 
				(documentRequestProcessor.httpRequest().secure() ? "s" : "") + 
				"://" + 
				documentRequestProcessor.httpRequest().host() + 
				scriptExecutionEnvironment.toSocketUri();
			
			Element jjScript = 
				makeScriptTag(documentRequestProcessor.document(), resourceFinder.findResource(AssetResource.class, JJ_JS).uri())
				.attr("id", "jj-connector-script")
				.attr("data-jj-socket-url", wsURI)
				.attr(
					"data-jj-startup-messages", 
					context.documentRequestProcessor().startupJJMessages().toString()
				);
			
			if (configuration.get(DocumentConfiguration.class).clientDebug()) {
				jjScript.attr("data-jj-debug", "true");
			}
			
			addScript(documentRequestProcessor.document(), jjScript);
			
			// associated scripts
			if (scriptExecutionEnvironment.sharedScriptResource() != null) {
				addScript(documentRequestProcessor.document(), ScriptResourceType.Shared.suffix(scriptExecutionEnvironment.toUri()));
			}
			if (scriptExecutionEnvironment.clientScriptResource() != null) {
				addScript(documentRequestProcessor.document(), ScriptResourceType.Client.suffix(scriptExecutionEnvironment.toUri()));
			}
		}
	}

	public boolean needsIO(final DocumentRequestProcessor documentRequestProcessor) {
		return false;
	}
}
