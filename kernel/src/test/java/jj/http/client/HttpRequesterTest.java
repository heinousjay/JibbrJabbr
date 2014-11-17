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
package jj.http.client;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.concurrent.GenericFutureListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpRequesterTest {
	
	@Mock HttpClient client;
	
	@Mock ChannelFuture future;
	
	@Spy EmbeddedChannel ch;
	
	@Captor ArgumentCaptor<GenericFutureListener<ChannelFuture>> listenerCaptor;
	
	@Captor ArgumentCaptor<HttpRequest> requestCaptor;
	
	@Test
	public void test() throws Exception {
		
		// given
		HttpRequester requester = new HttpRequester(client);
		given(client.connect(false, "foaas.com", 80)).willReturn(future);
		given(future.channel()).willReturn(ch);
		final AtomicBoolean called = new AtomicBoolean();
		
		// when
		requester.requestTo("http://foaas.com/off/:to/:from")
			.param("to", "joe")
			.param("from", "iou")
			.param("me", "awesome")
			.get()
			.header(HttpHeaders.Names.ACCEPT, "application/json")
			.begin(new HttpResponseListener() {
				@Override
				protected void responseComplete(HttpHeaders trailingHeaders) {
					called.set(true);
				}
			});
		
		// then
		verify(future).addListener(listenerCaptor.capture());
		
		// when
		listenerCaptor.getValue().operationComplete(future);
		
		// then
		verify(ch).writeAndFlush(requestCaptor.capture());
		
		HttpRequest request = requestCaptor.getValue();
		
		assertThat(request.uri(), is("/off/joe/iou?me=awesome"));
		assertThat(request.headers().get(HttpHeaders.Names.HOST), is("foaas.com"));
		assertThat(request.headers().get(HttpHeaders.Names.ACCEPT), is("application/json"));
		
		// when
		ch.pipeline().removeFirst();
		ch.writeInbound(new DefaultLastHttpContent());
		assertTrue(called.get());
	}

}
