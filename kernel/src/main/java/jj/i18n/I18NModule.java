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
package jj.i18n;

import jj.JJModule;
import jj.i18n.ScriptMessages.ScriptMessagesLoaderBundle;

/**
 * @author jason
 *
 */
public class I18NModule extends JJModule {

	@Override
	protected void configure() {

		bindAPIModulePath("/jj/i18n/api");

		bindConfiguration(I18NConfiguration.class);

		bindConverter(StringToLocaleConverter.class);

		bindContinuationProcessingOf(ScriptMessagesLoaderBundle.class).to(ScriptMessages.class);

		bindCreationOf(PropertiesResource.class).to(PropertiesResourceCreator.class);
		bindCreationOf(MessagesResource.class).to(MessagesResourceCreator.class);
	}

}
