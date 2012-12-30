package jj.document;

import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

import jj.JJExecutors;
import jj.resource.PropertiesResource;
import jj.resource.ResourceFinder;

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
public class InlineMessagesFilter implements DocumentFilter {
	
	private final ResourceFinder resourceFinder;
	
	private final JJExecutors executors;
	
	public InlineMessagesFilter(
		final ResourceFinder resourceFinder,
		final JJExecutors executors
	) {
		this.resourceFinder = resourceFinder;
		this.executors = executors;
	}
	
	@Override
	public void filter(final DocumentRequest documentRequest) {
		
		String baseName = documentRequest.htmlResource().baseName();
		
		PropertiesResource resource = 
			executors.isIOThread() ?	
			resourceFinder.loadResource(PropertiesResource.class, baseName) :
			resourceFinder.findResource(PropertiesResource.class, baseName);
			
		if (resource != null) {
			final PropertyResourceBundle bundle = resource.properties();
			for (String key : bundle.keySet()) {
				Elements e = documentRequest.document().select("[data-i18n=" + key + "]");
				if (!e.isEmpty()) {
					e.html(bundle.getString(key)).removeAttr("data-i18n");
				}
			}
			final String KEY = "data-i18n-";
			for (final Element el : documentRequest.document().select("[^" + KEY + "]")) {
				for (final Attribute attr : el.attributes()) {
					if (attr.getKey().startsWith(KEY)) {
						String newAttr = attr.getKey().substring(KEY.length());
						try {
							el.attr(newAttr, bundle.getString(attr.getValue()))
								.removeAttr(attr.getKey());
						} catch (MissingResourceException mre) {
							// FUCK YOU JAVA
						}
					}
				}
			}
		}
	}
	
	public boolean needsIO(final DocumentRequest documentRequest) {
		return resourceFinder.findResource(
			PropertiesResource.class, 
			documentRequest.htmlResource().baseName()
		) == null;
	}

}
