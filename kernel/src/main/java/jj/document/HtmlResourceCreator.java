package jj.document;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.SimpleResourceCreator;

@Singleton
public class HtmlResourceCreator extends SimpleResourceCreator<HtmlResource, Void> {
	
	@Inject
	HtmlResourceCreator(final Dependencies dependencies) {
		super(dependencies);
	}
}
