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

import static org.mockito.BDDMockito.*;

import java.net.InetSocketAddress;

import jj.DateFormatHelper;
import jj.ExecutionTrace;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

import org.junit.Ignore;
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
public class JJExecutiveHttpHandlerTest {
	
	private static final InetSocketAddress REMOTE_ADDRESS = new InetSocketAddress(80);
	private static final String URI = "/slick!";
	private static final String LENGTH = "3493459";
	private static final HttpResponseStatus STATUS = HttpResponseStatus.NOT_FOUND;
	private static final String REFERRER = "slappy!";
	private static final String USER_AGENT = "reticules";
	
	@Mock Logger logger;
	@Mock ExecutionTrace trace;
	@Mock FullHttpRequest request;
	@Mock Channel channel;
	@Mock Logger access;

	// ignored because i'm only keeping this 
	// component around to save the access 
	// logging code so i don't need to rewrite it
	@Ignore @Test
	public void test() throws Exception {
		
		// given
		//JJExecutiveHttpHandler h = new JJExecutiveHttpHandler(logger, trace);
		willReturn(true).given(logger).isTraceEnabled();
		given(channel.remoteAddress()).willReturn(REMOTE_ADDRESS);
		
		JJHttpRequest jjrequest = 
			new JJHttpRequest(request, channel)
			.header(HttpHeaders.Names.USER_AGENT, USER_AGENT)
			.header(HttpHeaders.Names.REFERER, REFERRER)
			.method(HttpMethod.GET)
			.uri(URI);
		
		JJHttpResponse response = new JJHttpResponse(jjrequest, channel, access);
		response.status(STATUS);
		
		// when
		// this is a little weird but it ensures agreement on the date
		String date1 = null;
		String date2 = null;
		
		while (date1 == null || !date1.equals(date2)) {
			date1 = DateFormatHelper.nowInAccessLogFormat();
			//h.handleHttpRequest(jjrequest, response);
			date2 = DateFormatHelper.nowInAccessLogFormat();
		}
		
		// then
		verify(logger).info("{} - - {} \"{} {} HTTP/1.1\" {} {} {} \"{}\"", 
			(REMOTE_ADDRESS instanceof InetSocketAddress) ? ((InetSocketAddress)REMOTE_ADDRESS).getAddress().getHostAddress() : REMOTE_ADDRESS.toString(),
			date1,
			HttpMethod.GET.toString(),
			URI,
			STATUS,
			LENGTH,
			"\"" + REFERRER + "\"",
			USER_AGENT
		);

		verify(logger).trace("Request Headers");
		verify(logger).trace("Response Headers");
		verify(logger).trace(HttpHeaders.Names.USER_AGENT + " : " + USER_AGENT);
		verify(logger).trace(HttpHeaders.Names.REFERER + " : " + REFERRER);
		verify(logger).trace(HttpHeaders.Names.CONTENT_LENGTH + " : " + LENGTH);
	}

}
