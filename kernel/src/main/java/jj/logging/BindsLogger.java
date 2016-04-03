package jj.logging;

import jj.HasBinder;

import java.lang.annotation.Annotation;

/**
 * Mix-in to help binding loggers
 *
 * Created by jasonmiller on 4/3/16.
 */
public interface BindsLogger extends HasBinder {

	default LoggingBinder.BindingBuilder bindLoggedEventsAnnotatedWith(Class<? extends Annotation> annotation) {
		return new LoggingBinder(binder()).annotatedWith(annotation);
	}
}
