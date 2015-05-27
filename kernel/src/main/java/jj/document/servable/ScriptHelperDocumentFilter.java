package jj.document.servable;

import static jj.system.ServerLocation.*;
import static jj.document.DocumentScriptEnvironment.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.document.DocumentConfiguration;
import jj.document.DocumentScriptEnvironment;
import jj.http.server.resource.StaticResource;
import jj.resource.ResourceFinder;

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

	private final DocumentConfiguration configuration;
	private final ResourceFinder resourceFinder;

	@Inject
	public ScriptHelperDocumentFilter(
		final DocumentConfiguration configuration,
		final ResourceFinder resourceFinder
	) {
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
		document.select("body").append(scriptTag.toString());
	}

	private void addScript(Document document, String uri) {
		addScript(document, makeScriptTag(document, uri));
	}

	@Override
	public void filter(final DocumentRequestProcessor documentRequestProcessor) {
		DocumentScriptEnvironment scriptEnvironment = documentRequestProcessor.documentScriptEnvironment();
		if (scriptEnvironment != null && scriptEnvironment.hasServerScript()) {
			
			// internal version of jquery
			// it's versioned already, so no need for sha-ing
			StaticResource jquery = resourceFinder.findResource(StaticResource.class, Assets, JQUERY_JS);
			jquery.addDependent(documentRequestProcessor.documentScriptEnvironment());
			addScript(documentRequestProcessor.document(), "/" + jquery.name());
			
			// jj script
			String wsURI = "ws" + 
				(documentRequestProcessor.httpRequest().secure() ? "s" : "") + 
				"://" + 
				documentRequestProcessor.httpRequest().host() + 
				scriptEnvironment.socketUri();
			
			StaticResource jj = resourceFinder.findResource(StaticResource.class, Assets, JJ_JS);
			jj.addDependent(documentRequestProcessor.documentScriptEnvironment());
			Element jjScript = 
				makeScriptTag(documentRequestProcessor.document(), jj.serverPath())
				.attr("id", "jj-connector-script")
				.attr("data-jj-socket-url", wsURI)
				.attr(
					"data-jj-startup-messages", 
					documentRequestProcessor.startupJJMessages().toString()
				);
			
			if (configuration.clientDebug()) {
				jjScript.attr("data-jj-debug", "true");
			}
			
			addScript(documentRequestProcessor.document(), jjScript);
			
			// associated scripts
			if (scriptEnvironment.sharedScriptResource() != null) {
				addScript(documentRequestProcessor.document(), scriptEnvironment.sharedScriptResource().serverPath());
			}
			if (scriptEnvironment.clientScriptResource() != null) {
				addScript(documentRequestProcessor.document(), scriptEnvironment.clientScriptResource().serverPath());
			}
		}
	}

	public boolean needsIO(final DocumentRequestProcessor documentRequestProcessor) {
		return false;
	}
}
