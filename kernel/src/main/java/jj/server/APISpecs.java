package jj.server;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.ResourceResolver;

@Singleton
public class APISpecs extends InternalAssets {

	@Inject
	APISpecs(ResourceResolver resolver, @APISpecPaths Set<String> paths) {
		super(resolver, paths);
	}
}
