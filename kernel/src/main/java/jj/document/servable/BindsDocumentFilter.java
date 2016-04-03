package jj.document.servable;

import com.google.inject.multibindings.Multibinder;
import jj.HasBinder;

/**
 * Mix-in to bind document filters
 *
 * Created by jasonmiller on 4/3/16.
 */
interface BindsDocumentFilter extends HasBinder {

	default void bindDocumentFilter(Class<? extends DocumentFilter> filter) {
		Multibinder.newSetBinder(binder(), DocumentFilter.class).addBinding().to(filter);
	}
}
