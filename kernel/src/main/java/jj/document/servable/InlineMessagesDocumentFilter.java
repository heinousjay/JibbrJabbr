package jj.document.servable;

import static jj.server.ServerLocation.Virtual;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.execution.CurrentTask;
import jj.i18n.MessagesResource;
import jj.resource.ResourceFinder;
import jj.resource.ResourceTask;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;

/**
 * Performs properties substitutions in place based on attribute values.
 * for example, given a properties file like
 * <pre>
 * hi=Why, hello there
 * goodbye=http://www.google.com/
 * </pre>
 * and html like
 * <pre>
 * &lt;a data-i18n-href="goodbye" data-i18n="hi">HI MESSAGE HERE&lt;/a>
 * </pre>
 * 
 * you get back
 * <pre>
 * &lt;a href="http://www.google.com/">Why, hello there&lt;/a>
 * </pre>
 * <p>
 * At some point in the future, this should also perform internal
 * substitutions but i'm not yet sure how
 * </p>
 * @author jason
 *
 */
@Singleton
class InlineMessagesDocumentFilter implements DocumentFilter {
	
	static final String MISSING_KEY = "??? MISSING KEY (%s) ???";
	
	private static final String TEXT_KEY = "data-i18n";
	
	private static final String ATTRIBUTE_KEY = TEXT_KEY + "-";

	private final ResourceFinder resourceFinder;
	
	private final CurrentTask currentTask;
	
	@Inject
	InlineMessagesDocumentFilter(
		final ResourceFinder resourceFinder,
		final CurrentTask currentTask
	) {
		this.resourceFinder = resourceFinder;
		this.currentTask = currentTask;
	}
	
	private String findValue(String key, MessagesResource resource) {
		return resource.containsKey(key) ? resource.message(key) : String.format(MISSING_KEY, key);
	}
	
	@Override
	public void filter(final DocumentRequestProcessor documentRequestProcessor) {
		
		String baseName = documentRequestProcessor.baseName();
		
		MessagesResource resource = resource(baseName, Locale.US);
		
		if (resource != null) {
			resource.addDependent(documentRequestProcessor.documentScriptEnvironment());
			
			for (final Element el : documentRequestProcessor.document().select("[" + TEXT_KEY + "]")) {
				String key = el.attr(TEXT_KEY);
				String value = findValue(key, resource);
				el.html(value).removeAttr(TEXT_KEY);
			}
			
			for (final Element el : documentRequestProcessor.document().select("[^" + ATTRIBUTE_KEY + "]")) {
				for (final Attribute attr : el.attributes()) {
					if (attr.getKey().startsWith(ATTRIBUTE_KEY)) {
						
						String key = attr.getValue();
						String value = findValue(key, resource);
						
						String newAttr = attr.getKey().substring(ATTRIBUTE_KEY.length());
						el.attr(newAttr, value).removeAttr(attr.getKey());
					}
				}
			}
		}
	}
	
	@Override
	public boolean needsIO(final DocumentRequestProcessor documentRequestProcessor) {
		return resourceFinder.findResource(
			MessagesResource.class, 
			Virtual,
			documentRequestProcessor.baseName(),
			Locale.US
		) == null;
	}
	
	private MessagesResource resource(String baseName, Locale locale) {
		return currentTask.currentIs(ResourceTask.class) ?	
			resourceFinder.loadResource(MessagesResource.class, Virtual, baseName, locale) :
			resourceFinder.findResource(MessagesResource.class, Virtual, baseName, locale);
	}

}
