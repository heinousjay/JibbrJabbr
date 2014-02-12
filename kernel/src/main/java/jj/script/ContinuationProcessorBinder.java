/*
 *    Copyright 2012 Jason Miller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jj.script;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;

public class ContinuationProcessorBinder {
	
	private final MapBinder<Class<? extends Continuation>, ContinuationProcessor> continuationProcessorBinder;
	
	public ContinuationProcessorBinder(final Binder binder) {
		continuationProcessorBinder = MapBinder.newMapBinder(
			binder,
			new TypeLiteral<Class<? extends Continuation>>() {},
			new TypeLiteral<ContinuationProcessor>() {}
		);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Continuation, U extends ContinuationProcessor> LinkedBindingBuilder<U> continuationOf(Class<T> key) {
		return (LinkedBindingBuilder<U>)continuationProcessorBinder.addBinding(key);
	}
}