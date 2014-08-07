package jj.i18n;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.SimpleResourceCreator;

@Singleton
class PropertiesResourceCreator extends SimpleResourceCreator<PropertiesResource> {

	@Inject
	PropertiesResourceCreator(final Dependencies dependencies) {
		super(dependencies);
	}
}
