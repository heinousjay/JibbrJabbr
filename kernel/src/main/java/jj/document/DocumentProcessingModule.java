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
package jj.document;

import jj.JJModule;
import jj.configuration.BindsConfiguration;
import jj.document.servable.DocumentServableModule;
import jj.http.server.websocket.WebSocketMessageProcessor;
import jj.jjmessage.JJMessage;
import jj.resource.BindsResourceCreation;
import jj.server.BindsServerPath;

/**
 * FIXME: WebSocketMessageProcessor binding doesn't really make any sense!
 * 
 * @author jason
 *
 */
public class DocumentProcessingModule extends JJModule
	implements BindsConfiguration,
		BindsMessageProcessor,
		BindsResourceCreation,
	BindsServerPath {

	@Override
	protected void configure() {

		processJJMessage(JJMessage.Type.Event).using(EventMessageProcessor.class);
		processJJMessage(JJMessage.Type.Element).using(ElementMessageProcessor.class);
		processJJMessage(JJMessage.Type.Result).using(ResultMessageProcessor.class);

		createResource(DocumentScriptEnvironment.class).using(DocumentScriptEnvironmentCreator.class);
		createResource(HtmlResource.class).using(HtmlResourceCreator.class);
		
		bind(WebSocketMessageProcessor.class).to(DocumentWebSocketMessageProcessors.class);
		
		bindConfiguration(DocumentConfiguration.class);
		
		bindAssetPath("/jj/document/assets");
		bindAPIModulePath("/jj/document/api");

		install(new DocumentServableModule());
	}

}
