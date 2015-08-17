package jj.script.module;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.SimpleResourceCreator;

@Singleton
public class ScriptResourceCreator extends SimpleResourceCreator<Void, ScriptResource> {

	@Inject
	ScriptResourceCreator(final Dependencies dependencies) {
		super(dependencies);
	}
}
