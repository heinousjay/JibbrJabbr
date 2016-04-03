package jj.script;

import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import jj.HasBinder;

/**
 * Mix-in to help with continuation processor binding
 *
 * Created by jasonmiller on 4/3/16.
 */
public interface BindsContinuationProcessing extends HasBinder {

	interface Using {
		void using(Class<? extends ContinuationProcessor> continuationProcessorClass);
	}

	default Using processorContinuation(Class<? extends Continuation> continuationClass) {
		return continuationProcessorClass ->
			MapBinder.newMapBinder(
				binder(),
				new TypeLiteral<Class<? extends Continuation>>() {},
				new TypeLiteral<ContinuationProcessor>() {}
			)
			.addBinding(continuationClass).to(continuationProcessorClass);
	}
}
