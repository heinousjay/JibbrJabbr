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

import com.google.inject.Binder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;

import jj.JJModule;
import jj.document.servable.DocumentServableModule;
import jj.http.server.websocket.WebSocketMessageProcessor;
import jj.jjmessage.JJMessage;
import jj.jjmessage.JJMessage.Type;

/**
 * FIXME: WebSocketMessageProcessor binding doesn't really make any sense!
 * 
 * @author jason
 *
 */
public class DocumentProcessingModule extends JJModule {
	
	public static class MessageProcessorBinder {
		
		private final MapBinder<Type, DocumentWebSocketMessageProcessor> messageProcessorsBinding;
		
		public MessageProcessorBinder(final Binder binder) {
			messageProcessorsBinding = 
				MapBinder.newMapBinder(binder, Type.class, DocumentWebSocketMessageProcessor.class);
		}

		@SuppressWarnings("unchecked")
		public <T extends Enum<Type>, U extends DocumentWebSocketMessageProcessor> LinkedBindingBuilder<U> of(Type type) {
			return (LinkedBindingBuilder<U>)messageProcessorsBinding.addBinding(type);
		}
	}

	@Override
	protected void configure() {
		
		MessageProcessorBinder bindProcessing = new MessageProcessorBinder(binder());
		
		bindProcessing.of(JJMessage.Type.Event).to(EventMessageProcessor.class);
		bindProcessing.of(JJMessage.Type.Element).to(ElementMessageProcessor.class);
		bindProcessing.of(JJMessage.Type.Result).to(ResultMessageProcessor.class);
		
		bindCreationOf(DocumentScriptEnvironment.class).to(DocumentScriptEnvironmentCreator.class);
		bindCreationOf(HtmlResource.class).to(HtmlResourceCreator.class);
		
		bind(WebSocketMessageProcessor.class).to(DocumentWebSocketMessageProcessors.class);
		
		bindConfiguration(DocumentConfiguration.class);
		
		bindAPIModulePath("/jj/document/api");

		install(new DocumentServableModule());
	}

}
