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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.nio.channels.FileChannel;

import jj.Version;
import jj.event.Publisher;
import jj.http.server.HttpServerRequestImpl;
import jj.http.server.HttpServerResponseImpl;
import jj.logging.LoggedEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpServerResponseImplTest {
	
	final String sha1 = "this is not really a sha";
	final String mime = "this is not really a mime";
	final ByteBuf bytes = Unpooled.wrappedBuffer("this is the bytes".getBytes(UTF_8));
	final long size = bytes.readableBytes();
	String host = "hostname";
	
	DefaultFullHttpRequest nettyRequest;
	HttpServerRequestImpl request;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS) ChannelHandlerContext ctx;
	@Mock Publisher publisher;
	@Mock Version version;
	HttpServerResponseImpl response;
	
	@Captor ArgumentCaptor<LoggedEvent> eventCaptor;

	@Before
	public void before() {
		nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
		nettyRequest.headers().add(HttpHeaderNames.HOST, host);
		request = new HttpServerRequestImpl(nettyRequest, ctx);
		
		response = new HttpServerResponseImpl(version, request, ctx, publisher);
		assertThat(response.charset(), is(UTF_8));
	}

	private void testCachedResource(ServableResource resource) throws IOException {
		response.sendCachableResource(resource);
		
		assertThat(response.status(), is(HttpResponseStatus.OK));
		assertThat(response.header(HttpHeaderNames.CONTENT_TYPE), is(mime));
		assertThat(response.header(HttpHeaderNames.ETAG), is(sha1));
		assertThat(response.header(HttpHeaderNames.CONTENT_LENGTH), is(String.valueOf(size)));
		assertThat(response.header(HttpHeaderNames.CACHE_CONTROL), is(HttpServerResponse.MAX_AGE_ONE_YEAR));
	}

	private void testUncachedResource(ServableResource resource) throws IOException {
		response.sendUncachableResource(resource);
		
		assertThat(response.status(), is(HttpResponseStatus.OK));
		assertThat(response.header(HttpHeaderNames.CONTENT_TYPE), is(mime));
		assertThat(response.header(HttpHeaderNames.ETAG), is(sha1));
		assertThat(response.header(HttpHeaderNames.CONTENT_LENGTH), is(String.valueOf(size)));
		assertThat(response.header(HttpHeaderNames.CACHE_CONTROL), is(HttpHeaderValues.NO_CACHE.toString()));
	}

	private void testCachedNotModifiedResource(ServableResource resource) throws IOException {
		response.sendNotModified(resource, true);
		
		assertThat(response.status(), is(HttpResponseStatus.NOT_MODIFIED));
		assertThat(response.header(HttpHeaderNames.ETAG), is(sha1));
		assertThat(response.header(HttpHeaderNames.CACHE_CONTROL), is(HttpServerResponse.MAX_AGE_ONE_YEAR));
		assertThat(response.hasNoBody(), is(true));
	}

	private void testUncachedNotModifiedResource(ServableResource resource) throws IOException {
		response.sendNotModified(resource);
		
		assertThat(response.status(), is(HttpResponseStatus.NOT_MODIFIED));
		assertThat(response.header(HttpHeaderNames.ETAG), is(sha1));
		assertThat(response.containsHeader(HttpHeaderNames.CACHE_CONTROL), is(false));
		assertThat(response.hasNoBody(), is(true));
	}
	
	private void verifyInlineResponse() {
		
		verify(ctx, times(2)).write(any());
		verify(ctx).writeAndFlush(any());
		
		verifyNoMoreInteractions(ctx);
		
		verifyRequestRespondedIsPublished();
	}
	
	private void verifyRequestRespondedIsPublished() {
		// since we ALWAYS respond, every test will call this, so
		// we capture all publications here
		verify(publisher, atLeastOnce()).publish(eventCaptor.capture());
		verifyEventIsPublished(RequestResponded.class);
	}
	
	private void verifyEventIsPublished(Class<?> eventType) {
		boolean found = false;
		for (LoggedEvent event : eventCaptor.getAllValues()) {
			if (eventType.isInstance(event)) {
				found = true;
				break;
			}
		}
		assertTrue("could not verify the RequestResponded event was properly fired", found);
	}
	
	@Test
	public void testNotFound() {
		
		response.sendNotFound();
		
		assertThat(response.status(), is(HttpResponseStatus.NOT_FOUND));
		verifyInlineResponse();
	}
	
	@Test
	public void testError() {
		
		Exception e = new Exception();
		
		response.error(e);
		
		assertThat(response.status(), is(HttpResponseStatus.INTERNAL_SERVER_ERROR));
		verifyInlineResponse();
		verifyEventIsPublished(RequestErrored.class);
	}
	
	LoadedResource givenALoadedResource() throws IOException {
		
		LoadedResource lr = mock(LoadedResource.class);
		given(lr.sha1()).willReturn(sha1);
		given(lr.contentType()).willReturn(mime);
		given(lr.size()).willReturn(size);
		given(lr.bytes()).willReturn(bytes);
		return lr;
	}

	@Test
	public void testCachedLoadedResource() throws IOException {
		
		testCachedResource(givenALoadedResource());
		verifyInlineResponse();
	}
	
	@Test
	public void testUncachedLoadedResource() throws IOException {
		
		testUncachedResource(givenALoadedResource());
		verifyInlineResponse();
	}
	
	@Test
	public void testCachedNotModifiedLoadedResource() throws IOException {
		
		testCachedNotModifiedResource(givenALoadedResource());
		verifyInlineResponse();
	}
	
	@Test
	public void testUncachedNotModifiedLoadedResource() throws IOException {
		
		testUncachedNotModifiedResource(givenALoadedResource());
		verifyInlineResponse();
	}
	
	TransferableResource givenATransferableResource() throws IOException {
		
		TransferableResource tr = mock(TransferableResource.class);
		given(tr.sha1()).willReturn(sha1);
		given(tr.contentType()).willReturn(mime);
		given(tr.size()).willReturn(size);
		given(tr.fileChannel()).willReturn(mock(FileChannel.class));
		
		return tr;
	}

	private void verifyTransferredResponse() {

		// this verifies the response write, and the file region write
		verify(ctx, times(2)).write(any());
		// this verifies the LastHttpContent
		verify(ctx).writeAndFlush(anyObject());
		
		verifyNoMoreInteractions(ctx);
		
		verifyRequestRespondedIsPublished();
	}
	
	@Test
	public void testCachedTransferableResource() throws IOException {
		
		testCachedResource(givenATransferableResource());
		verifyTransferredResponse();
	}
	
	@Test
	public void testUncachedTransferableResource() throws IOException {
		
		testUncachedResource(givenATransferableResource());
		verifyTransferredResponse();
	}
	
	@Test
	public void testCachedNotModifiedTransferableResource() throws IOException {
		
		testCachedNotModifiedResource(givenATransferableResource());
		verifyInlineResponse();
	}
	
	@Test
	public void testUncachedNotModifiedTransferableResource() throws IOException {
		
		testUncachedNotModifiedResource(givenATransferableResource());
		verifyInlineResponse();
	}
}
