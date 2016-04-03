package jj.execution;

import jj.HasBinder;

import javax.inject.Singleton;

/**
 * Mix-in to help binding an executor
 *
 * Created by jasonmiller on 4/3/16.
 */
public interface BindsExecutor extends HasBinder {
	default void bindExecutor(Class<?> executor) {
		assert executor.isAnnotationPresent(Singleton.class);
		binder().bind(executor).asEagerSingleton();
	}
}
