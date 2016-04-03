package jj.conversion;

import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import jj.HasBinder;

/**
 * Mix-in to bind converters to the conversion system
 *
 * Created by jasonmiller on 4/3/16.
 */
public interface BindsConverter extends HasBinder {

	default void bindConverter(Class<? extends Converter<?, ?>> converterClass) {
		Multibinder.newSetBinder(binder(), new TypeLiteral<Converter<?, ?>>() {}).addBinding().to(converterClass);
	}
}
