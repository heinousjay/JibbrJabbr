package jj.server;

import com.google.inject.multibindings.Multibinder;
import jj.HasBinder;

/**
 * Created by jasonmiller on 4/3/16.
 */
public interface BindsServerPath extends HasBinder {

	default void bindAssetPath(String path) {
		assert path != null && path.startsWith("/") : "path must be present and start with /";
		Multibinder.newSetBinder(binder(), String.class, AssetPaths.class).addBinding().toInstance(path);
	}

	default void bindAPIModulePath(String path) {
		assert path != null && path.startsWith("/") : "path must be present and start with /";
		Multibinder.newSetBinder(binder(), String.class, APIModulePaths.class).addBinding().toInstance(path);
	}

	default void bindAPISpecPath(String path) {
		assert path != null && path.startsWith("/") : "path must be present and start with /";
		Multibinder.newSetBinder(binder(), String.class, APISpecPaths.class).addBinding().toInstance(path);
	}
}
