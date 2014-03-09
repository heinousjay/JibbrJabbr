package jj.document.servable;

import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.AppLocation;
import jj.resource.IsThread;
import jj.resource.ResourceFinder;
import jj.resource.property.PropertiesResource;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
	
	@Override
	public void filter(final DocumentRequestProcessor documentRequestProcessor) {
		
		String baseName = documentRequestProcessor.baseName();
		
		PropertiesResource resource = 
			isThread.forResourceTask() ?	
			resourceFinder.loadResource(PropertiesResource.class, AppLocation.Base, baseName + ".properties") :
			resourceFinder.findResource(PropertiesResource.class, AppLocation.Base, baseName + ".properties");
			
		if (resource != null) {
			final PropertyResourceBundle bundle = resource.properties();
			for (String key : bundle.keySet()) {
				Elements e = documentRequestProcessor.document().select("[data-i18n=" + key + "]");
				if (!e.isEmpty()) {
					e.html(bundle.getString(key)).removeAttr("data-i18n");
				}
			}
			final String KEY = "data-i18n-";
			for (final Element el : documentRequestProcessor.document().select("[^" + KEY + "]")) {
				for (final Attribute attr : el.attributes()) {
					if (attr.getKey().startsWith(KEY)) {
						String newAttr = attr.getKey().substring(KEY.length());
						try {
							el.attr(newAttr, bundle.getString(attr.getValue()))
								.removeAttr(attr.getKey());
						} catch (MissingResourceException mre) {
							// thanks java
						}
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