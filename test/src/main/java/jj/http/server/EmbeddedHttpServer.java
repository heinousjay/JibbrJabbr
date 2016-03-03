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
import io.netty.util.ReferenceCountUtil;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import jj.ServerLogger;
import jj.event.Publisher;
import jj.execution.TaskRunner;
import jj.logging.Emergency;
import jj.logging.LoggedEvent;
import org.slf4j.Logger;

/**
 * Provides a hook into the HTTP system
 *
 * @author jason
 *
 */
@Singleton
public class EmbeddedHttpServer {
	
	private final Provider<EngineHttpHandler> handlerProvider;
	
	private final TaskRunner taskRunner;

	private final Publisher publisher;
	
	@Inject
	EmbeddedHttpServer(
		Provider<EngineHttpHandler> handlerProvider,
		TaskRunner taskRunner,
	    Publisher publisher
	) {
		this.handlerProvider = handlerProvider;
		this.taskRunner = taskRunner;
		this.publisher = publisher;
	}
	
	public void request(EmbeddedHttpRequest request, EmbeddedHttpResponse.ResponseReady responseReady) {
		init(request, new EmbeddedHttpResponse(responseReady));
	}

	public EmbeddedHttpResponse request(EmbeddedHttpRequest request) {
		return init(request, new EmbeddedHttpResponse());
	}

	private EmbeddedHttpResponse init(EmbeddedHttpRequest request, EmbeddedHttpResponse response) {

		final EmbeddedChannel channel = new EmbeddedChannel(new ReceiverAdapter(response), handlerProvider.get());
		
		final FullHttpRequest msg = request.fullHttpRequest();
		taskRunner.execute(new HttpServerTask("submitting embedded request for " + request.request.uri()) {
			
			@Override
			protected void run() throws Exception {
				channel.writeInbound(msg);
			}
		});

		publisher.publish(request);
		
		return response;
	}

	@ServerLogger
	private abstract static class EmbeddedResponseWrite extends LoggedEvent {}
	
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
			response.buffer.addComponent(ReferenceCountUtil.releaseLater(content.retain()));
			response.buffer.writerIndex(response.buffer.writerIndex() + content.writerIndex());
		}
		
		@Override
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

			publisher.publish(new EmbeddedResponseWrite() {
				@Override
				public void describeTo(Logger logger) {
					logger.trace("writing embedded http message of type {}", msg.getClass());
				}
			});

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
					ByteBuf buffer = fileChunk.readChunk(ctx.alloc());
					
					if (buffer == null) {
						Thread.sleep(10);
					} else {
						addBodyComponent(buffer);
					}
					
				} while (!fileChunk.isEndOfInput());
				
			} else {
				publisher.publish(new Emergency("received a message of unknown type {}", msg.getClass()));
			}
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			response.error = cause;
		}
	}
}
