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
package jj.http.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedNioFile;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import jj.execution.TaskRunner;

/**
 * @author jason
 *
 */
@Singleton
public class EmbeddedHttpServer {
	
	private final Provider<EngineHttpHandler> handlerProvider;
	
	private final TaskRunner taskRunner;
	
	public interface ResponseReady {
		void ready(EmbeddedHttpResponse response);
	}
	
	@Inject
	EmbeddedHttpServer(
		final Provider<EngineHttpHandler> handlerProvider,
		final TaskRunner taskRunner
	) {
		this.handlerProvider = handlerProvider;
		this.taskRunner = taskRunner;
	}
	
	public void request(EmbeddedHttpRequest request, ResponseReady responseReady) {
		init(request, new EmbeddedHttpResponse(responseReady));
	}

	public EmbeddedHttpResponse request(EmbeddedHttpRequest request) {
		return init(request, new EmbeddedHttpResponse());
	}
	
	private EmbeddedHttpResponse init(EmbeddedHttpRequest request, EmbeddedHttpResponse response) {

		final EmbeddedChannel channel = new EmbeddedChannel(new ReceiverAdapter(response), handlerProvider.get());
		
		final FullHttpRequest msg = request.fullHttpRequest();
		taskRunner.execute(new HttpTask("submitting embedded request for " + request.request.getUri()) {
			
			@Override
			protected void run() throws Exception {
				channel.writeInbound(msg);
			}
		});
		
		
		return response;
	}
	
	private final class ReceiverAdapter extends ChannelOutboundHandlerAdapter {
		
		private final EmbeddedHttpResponse response;
		
		ReceiverAdapter(final EmbeddedHttpResponse response) {
			this.response = response;
		}
		
		@Override
		public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
			response.responseReady();
		}
		
		private void addBodyComponent(ByteBuf content) {
			response.buffer.addComponent(content.retain());
			response.buffer.writerIndex(response.buffer.writerIndex() + content.writerIndex());
		}
		
		@Override
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

			if (msg instanceof HttpResponse) {
				
				response.response = (HttpResponse)msg;
				
			} else if (msg instanceof HttpContent) {
				
				addBodyComponent(((HttpContent)msg).content());
				
				if (msg instanceof LastHttpContent) {
					// also handle trailing headers! and then
					ctx.channel().close();
				}
				
			} else if (msg instanceof ByteBuf) {
				
				addBodyComponent(((ByteBuf)msg));
				
			} else if (msg instanceof ChunkedNioFile) {
				
				ChunkedNioFile fileChunk = (ChunkedNioFile)msg;
				do {
					ByteBuf buffer = fileChunk.readChunk(ctx);
					
					if (buffer == null) {
						Thread.sleep(10);
					} else {
						addBodyComponent(buffer);
					}
					
				} while (!fileChunk.isEndOfInput());
				
			} else {
				System.out.println("yo yo yo! " + msg.getClass());
			}
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			response.error = cause;
		}
	}
}
