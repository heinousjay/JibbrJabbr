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
package jj.webdriver.panel.generator;

import java.util.ArrayList;
import java.util.List;

import jj.webdriver.panel.PanelMethodGenerator;

import com.google.inject.AbstractModule;

/**
 * <p>
 * configuration point for page method generators
 * 
 * @author jason
 *
 */
public class PanelMethodGeneratorsModule extends AbstractModule {
	
	private final List<Class<? extends PanelMethodGenerator>> generators = new ArrayList<>();
	
	PanelMethodGeneratorsModule add(Class<? extends PanelMethodGenerator> generator) {
		generators.add(generator);
		return this;
	}

	@Override
	protected void configure() {
		PanelMethodGeneratorBinder bindPanelMethodGenerator = new PanelMethodGeneratorBinder(binder());
		
		bindPanelMethodGenerator.to(SetInputMethodGenerator.class);
		bindPanelMethodGenerator.to(SetModelMethodGenerator.class);
		bindPanelMethodGenerator.to(ClickMethodGenerator.class);
		bindPanelMethodGenerator.to(GetPanelMethodGenerator.class);
		bindPanelMethodGenerator.to(ReadMethodGenerator.class);
		
		for (Class<? extends PanelMethodGenerator> generator : generators) {
			bindPanelMethodGenerator.to(generator);
		}
		
	}

}
