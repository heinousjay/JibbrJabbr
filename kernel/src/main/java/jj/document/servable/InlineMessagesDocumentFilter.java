package jj.document.servable;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.AppLocation;
import jj.messaging.PropertiesResource;
import jj.resource.IsThread;
import jj.resource.ResourceFinder;

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
	
	private final IsThread isThread;
	
	@Inject
	InlineMessagesDocumentFilter(
		final ResourceFinder resourceFinder,
		final IsThread isThread
	) {
		this.resourceFinder = resourceFinder;
		this.isThread = isThread;
	}
	
	private String findValue(String key, Map<String, String> bundle) {
		return bundle.containsKey(key) ? bundle.get(key) : String.format(MISSING_KEY, key);
	}
	
	@Override
	public void filter(final DocumentRequestProcessor documentRequestProcessor) {
		
		String baseName = documentRequestProcessor.baseName();
		
		PropertiesResource resource = 
			isThread.forResourceTask() ?	
			resourceFinder.loadResource(PropertiesResource.class, AppLocation.Base, baseName + ".properties") :
			resourceFinder.findResource(PropertiesResource.class, AppLocation.Base, baseName + ".properties");
			
		if (resource != null) {
			final Map<String, String> bundle = resource.properties();
			
			for (final Element el : documentRequestProcessor.document().select("[" + TEXT_KEY + "]")) {
				String key = el.attr(TEXT_KEY);
				String value = findValue(key, bundle);
				el.html(value).removeAttr(TEXT_KEY);
			}
			
			for (final Element el : documentRequestProcessor.document().select("[^" + ATTRIBUTE_KEY + "]")) {
				for (final Attribute attr : el.attributes()) {
					if (attr.getKey().startsWith(ATTRIBUTE_KEY)) {
						
						String key = attr.getValue();
						String value = findValue(key, bundle);
						
						String newAttr = attr.getKey().substring(ATTRIBUTE_KEY.length());
						el.attr(newAttr, value).removeAttr(attr.getKey());
					}
				}
			}
		}
	}
	
	public boolean needsIO(final DocumentRequestProcessor documentRequestProcessor) {
		return resourceFinder.findResource(
			PropertiesResource.class, 
			AppLocation.Base,
			documentRequestProcessor.baseName() + ".properties"
		) == null;
	}

}
