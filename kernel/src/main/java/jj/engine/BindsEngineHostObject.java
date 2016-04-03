package jj.engine;

import com.google.inject.multibindings.Multibinder;
import jj.HasBinder;

/**
 * Mix-in to bind engine host objects
 *
 * Created by jasonmiller on 4/3/16.
 */
public interface BindsEngineHostObject extends HasBinder {

	default void bindHostObject(Class<? extends HostObject> hostObjectClass) {
		Multibinder.newSetBinder(binder(), HostObject.class).addBinding().to(hostObjectClass);
	}
}
