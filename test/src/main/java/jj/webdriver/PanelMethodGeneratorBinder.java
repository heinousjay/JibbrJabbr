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
package jj.webdriver;

import com.google.inject.Binder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.multibindings.Multibinder;

/**
 * @author jason
 *
 */
class PanelMethodGeneratorBinder {
	
	private final Multibinder<PanelMethodGenerator> generatorBinder;
	
	PanelMethodGeneratorBinder(Binder binder) {
		generatorBinder = Multibinder.newSetBinder(binder, PanelMethodGenerator.class);
	}
	
	ScopedBindingBuilder to(Class<? extends PanelMethodGenerator> generator) {
		return generatorBinder.addBinding().to(generator);
	}
}
