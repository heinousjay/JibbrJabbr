package jj.application;

import jj.JJModule;
import jj.resource.BindsLocationResolution;

public class ApplicationModule extends JJModule implements BindsLocationResolution {

	@Override
	protected void configure() {
		resolvePathsFor(AppLocation.class).with(Application.class);
	}

}
