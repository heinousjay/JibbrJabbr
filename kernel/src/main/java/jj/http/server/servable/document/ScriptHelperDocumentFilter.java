package jj.http.server.servable.document;

import static jj.configuration.Assets.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.AppLocation;
import jj.configuration.Configuration;
import jj.resource.ResourceFinder;
import jj.resource.document.DocumentScriptEnvironment;
import jj.resource.script.ScriptResourceType;
import jj.resource.stat.ic.StaticResource;

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
	private final ResourceFinder resourceFinder;

	@Inject
	public ScriptHelperDocumentFilter(
		final Configuration configuration,
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
		document.select("head").append(scriptTag.toString());
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
			addScript(documentRequestProcessor.document(), "/" + resourceFinder.findResource(StaticResource.class, AppLocation.Assets, JQUERY_JS).name());
			
			// jj script
			String wsURI = "ws" + 
				(documentRequestProcessor.httpRequest().secure() ? "s" : "") + 
				"://" + 
				documentRequestProcessor.httpRequest().host() + 
				scriptEnvironment.socketUri();
			
			Element jjScript = 
				makeScriptTag(documentRequestProcessor.document(), resourceFinder.findResource(StaticResource.class, AppLocation.Assets, JJ_JS).uri())
				.attr("id", "jj-connector-script")
				.attr("data-jj-socket-url", wsURI)
				.attr(
					"data-jj-startup-messages", 
					documentRequestProcessor.startupJJMessages().toString()
				);
			
			if (configuration.get(DocumentConfiguration.class).clientDebug()) {
				jjScript.attr("data-jj-debug", "true");
			}
			
			addScript(documentRequestProcessor.document(), jjScript);
			
			// associated scripts
			if (scriptEnvironment.sharedScriptResource() != null) {
				addScript(documentRequestProcessor.document(), ScriptResourceType.Shared.suffix(scriptEnvironment.uri()));
			}
			if (scriptEnvironment.clientScriptResource() != null) {
				addScript(documentRequestProcessor.document(), ScriptResourceType.Client.suffix(scriptEnvironment.uri()));
			}
		}
	}

	public boolean needsIO(final DocumentRequestProcessor documentRequestProcessor) {
		return false;
	}
}
