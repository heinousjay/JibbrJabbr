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
package jj.http;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.nio.channels.FileChannel;

import jj.execution.ExecutionTrace;
import jj.resource.LoadedResource;
import jj.resource.Resource;
import jj.resource.TransferableResource;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class JJHttpResponseTest {
	
	final String sha1 = "this is not really a sha";
	final String mime = "this is not really a mime";
	final ByteBuf bytes = Unpooled.wrappedBuffer("this is the bytes".getBytes(UTF_8));
	final long size = bytes.readableBytes();
	
	DefaultFullHttpRequest nettyRequest;
	JJHttpRequest request;
	@Mock Channel channel;
	@Mock ChannelPromise p;
	@Mock Logger logger;
	@Mock ExecutionTrace trace;
	JJHttpResponse response;

	@Before
	public void before() {
		nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
		request = new JJHttpRequest(nettyRequest, channel);
		
		response = new JJHttpResponse(request, channel, logger, trace);
		assertThat(response.charset(), is(UTF_8));
		
		given(channel.newPromise()).willReturn(p);
	}

	private void testCachedResource(Resource resource) throws IOException {
		response.sendCachedResource(resource);
		
		assertThat(response.status(), is(HttpResponseStatus.OK));
		assertThat(response.header(HttpHeaders.Names.CONTENT_TYPE), is(mime));
		assertThat(response.header(HttpHeaders.Names.ETAG), is(sha1));
		assertThat(response.header(HttpHeaders.Names.CONTENT_LENGTH), is(String.valueOf(size)));
		assertThat(response.header(HttpHeaders.Names.CACHE_CONTROL), is(AbstractHttpResponse.MAX_AGE_ONE_YEAR));
	}

	private void testUncachedResource(Resource resource) throws IOException {
		response.sendUncachedResource(resource);
		
		assertThat(response.status(), is(HttpResponseStatus.OK));
		assertThat(response.header(HttpHeaders.Names.CONTENT_TYPE), is(mime));
		assertThat(response.header(HttpHeaders.Names.ETAG), is(sha1));
		assertThat(response.header(HttpHeaders.Names.CONTENT_LENGTH), is(String.valueOf(size)));
		assertThat(response.header(HttpHeaders.Names.CACHE_CONTROL), is(HttpHeaders.Values.NO_CACHE));
	}

	private void testCachedNotModifiedResource(Resource resource) throws IOException {
		response.sendNotModified(resource, true);
		
		assertThat(response.status(), is(HttpResponseStatus.NOT_MODIFIED));
		assertThat(response.header(HttpHeaders.Names.ETAG), is(sha1));
		assertThat(response.header(HttpHeaders.Names.CACHE_CONTROL), is(AbstractHttpResponse.MAX_AGE_ONE_YEAR));
		assertThat(response.hasNoBody(), is(true));
	}

	private void testUncachedNotModifiedResource(Resource resource) throws IOException {
		response.sendNotModified(resource);
		
		assertThat(response.status(), is(HttpResponseStatus.NOT_MODIFIED));
		assertThat(response.header(HttpHeaders.Names.ETAG), is(sha1));
		assertThat(response.containsHeader(HttpHeaders.Names.CACHE_CONTROL), is(false));
		assertThat(response.hasNoBody(), is(true));
	}
	
	@Test
	public void testNotFound() {
		
		response.sendNotFound();
		
		assertThat(response.response.getStatus(), is(HttpResponseStatus.NOT_FOUND));
		
		verify(channel).newPromise();
		verify(channel).write(response.response, p);
		verify(channel).flush();
		
		verifyNoMoreInteractions(channel);
	}
	
	LoadedResource givenALoadedResource() throws IOException {
		
		LoadedResource lr = mock(LoadedResource.class);
		given(lr.sha1()).willReturn(sha1);
		given(lr.mime()).willReturn(mime);
		given(lr.size()).willReturn(size);
		given(lr.bytes()).willReturn(bytes);
		return lr;
	}

	@Test
	public void testCachedLoadedResource() throws IOException {
		testCachedResource(givenALoadedResource());
		
		verify(channel).newPromise();
		verify(channel).write(response.response, p);
		verify(channel).flush();
		
		verifyNoMoreInteractions(channel);
	}
	
	@Test
	public void testUncachedLoadedResource() throws IOException {
		
		testUncachedResource(givenALoadedResource());
		
		verify(channel).newPromise();
		verify(channel).write(response.response, p);
		verify(channel).flush();
		
		verifyNoMoreInteractions(channel);
	}
	
	@Test
	public void testCachedNotModifiedLoadedResource() throws IOException {
		
		testCachedNotModifiedResource(givenALoadedResource());
		
		verify(channel).newPromise();
		verify(channel).write(response.response, p);
		verify(channel).flush();
		
		verifyNoMoreInteractions(channel);
	}
	
	@Test
	public void testUncachedNotModifiedLoadedResource() throws IOException {
		
		testUncachedNotModifiedResource(givenALoadedResource());
		
		verify(channel).newPromise();
		verify(channel).write(response.response, p);
		verify(channel).flush();
		
		verifyNoMoreInteractions(channel);
	}
	
	TransferableResource givenATransferableResource() throws IOException {
		
		TransferableResource tr = mock(TransferableResource.class);
		given(tr.sha1()).willReturn(sha1);
		given(tr.mime()).willReturn(mime);
		given(tr.size()).willReturn(size);
		given(tr.fileChannel()).willReturn(mock(FileChannel.class));
		
		return tr;
	}
	
	@Test
	public void testCachedTransferableResource() throws IOException {
		
		testCachedResource(givenATransferableResource());
		
		verify(channel).newPromise();
		verify(channel).write(response.response);
		// this verifies the response write, and the file region write
		verify(channel, times(2)).write(any());
		// this verifies the LastHttpContent and the promise
		verify(channel).write(anyObject(), eq(p));
		verify(channel).flush();
		
		verifyNoMoreInteractions(channel);
	}
	
	@Test
	public void testUncachedTransferableResource() throws IOException {
		
		testUncachedResource(givenATransferableResource());

		verify(channel).newPromise();
		verify(channel).write(response.response);
		// this verifies the response write, and the file region write
		verify(channel, times(2)).write(any());
		// this verifies the LastHttpContent and the promise
		verify(channel).write(anyObject(), eq(p));
		verify(channel).flush();
		
		verifyNoMoreInteractions(channel);
	}
	
	@Test
	public void testCachedNotModifiedTransferableResource() throws IOException {
		
		testCachedNotModifiedResource(givenATransferableResource());
		
		verify(channel).newPromise();
		verify(channel).write(response.response, p);
		verify(channel).flush();
		
		verifyNoMoreInteractions(channel);
	}
	
	@Test
	public void testUncachedNotModifiedTransferableResource() throws IOException {
		
		testUncachedNotModifiedResource(givenATransferableResource());
		
		verify(channel).newPromise();
		verify(channel).write(response.response, p);
		verify(channel).flush();
		
		verifyNoMoreInteractions(channel);
	}

}
