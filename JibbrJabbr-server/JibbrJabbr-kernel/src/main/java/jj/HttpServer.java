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
package jj;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.jboss.netty.channel.ChannelState.BOUND;
import static jj.KernelMessages.*;

import java.net.InetSocketAddress;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.UpstreamChannelStateEvent;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.cal10n.LocLogger;

public final class HttpServer {
	
	private final LocLogger logger;
	
	private final NettyRequestBridge requestHandler;
	
	private final KernelSettings kernelSettings;
	
	private final ServerBootstrap bootstrap;
	
	private final CyclicBarrier startBarrier = new CyclicBarrier(2);
	
	/** All channels currently in use by the server */
	private final ChannelGroup allChannels = 
		new DefaultChannelGroup(HttpServer.class.getName());
	
	/**
	 * Simple tracker for channels created by this server.
	 * May be expanded at some future point.
	 */
	private final ChannelHandler clientChannelTrackingHandler = new ChannelUpstreamHandler() {

		@Override
		public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {
			allChannels.add(e.getChannel());
			ctx.sendUpstream(e);
		}
		
	};
	
	private static final String CLIENT_TRACKER_NAME = "Client tracker";
	
	private final ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
		
		@Override
		public ChannelPipeline getPipeline() {
			ChannelPipeline pipeline = new DefaultChannelPipeline();
			pipeline.addLast(CLIENT_TRACKER_NAME, clientChannelTrackingHandler);
			// if SSL is enabled
			// pipeline.addLast(SslHandler.class.getSimpleName(), new SslHandler(engine));
			pipeline.addLast(HttpRequestDecoder.class.getSimpleName(), 
				new HttpRequestDecoder(
					kernelSettings.httpMaxInitialLineLength(),
					kernelSettings.httpMaxHeaderSize(),
					kernelSettings.httpMaxChunkSize()
				)
			);
			pipeline.addLast(HttpChunkAggregator.class.getSimpleName(),
				new HttpChunkAggregator(kernelSettings.httpMaxRequestContentLength())
			);
			pipeline.addLast(HttpResponseEncoder.class.getSimpleName(), new HttpResponseEncoder());
			pipeline.addLast(HttpContentCompressor.class.getSimpleName(), 
				new HttpContentCompressor(kernelSettings.httpCompressionLevel())
			);
			pipeline.addLast(ChunkedWriteHandler.class.getSimpleName(), new ChunkedWriteHandler());
			pipeline.addLast(requestHandler.getClass().getSimpleName(), requestHandler);
			
			return pipeline;
		}
	};
	
	public HttpServer(
		final LocLogger logger, 
		final NettyRequestBridge requestHandler,
		final KernelSettings kernelSettings,
		final SynchThreadPool bossExecutor,
		final AsyncThreadPool httpExecutor
	) {
		assert logger != null;
		assert requestHandler != null;
		assert kernelSettings != null;
		assert bossExecutor != null;
		
		this.logger = logger;
		this.requestHandler = requestHandler;
		this.kernelSettings = kernelSettings;
		
		logger.debug(ObjectInstantiating, HttpServer.class);
		
		bootstrap = new ServerBootstrap(
			new NioServerSocketChannelFactory(
				bossExecutor,
				httpExecutor,
				kernelSettings.asynchronousThreadMaxCount()
			)
		);
		
		bossExecutor.submit(initializer);
		
		
	}
	
	
	public void control(KernelControl control) {
		
		switch (control) {
		case Start:
			try {
				startBarrier.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				// publish the exception as an event.  BAM
				e.printStackTrace();
			}
			break;
			
		case Stop:
			logger.info(HttpServerResourcesReleasing);
			if (!allChannels.close().awaitUninterruptibly(kernelSettings.httpMaxShutdownTimeout(), SECONDS)) {
				logger.warn(ConnectionsRemainPastTimeout, kernelSettings.httpMaxShutdownTimeout());
			}
			// TODO kill this after moving the i/o threadpool out
			bootstrap.releaseExternalResources();
			logger.info(HttpServerResourcesReleased);
			break;
		}
		
	}

	private Runnable initializer = new Runnable() {
		
		@Override
		public void run() {
			bootstrap.setPipelineFactory(pipelineFactory);

			bootstrap.setParentHandler(new ChannelUpstreamHandler() {
				
				@Override
				public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {
					if (e instanceof UpstreamChannelStateEvent) {
						UpstreamChannelStateEvent ucse = (UpstreamChannelStateEvent)e;
						if (ucse.getState() == BOUND &&
						    ucse.getValue() != null) {
							allChannels.add(e.getChannel());
							logger.info(ReachedStartSyncPoint);
							startBarrier.await();
							logger.info(InterfaceBound, ((UpstreamChannelStateEvent)e).getValue());
						}
					}
					ctx.sendUpstream(e);
				}
			});
			
			int port = kernelSettings.port();
			logger.debug(BindingPort, port);
			bootstrap.bind(new InetSocketAddress(port));
		}
	};
	
}
