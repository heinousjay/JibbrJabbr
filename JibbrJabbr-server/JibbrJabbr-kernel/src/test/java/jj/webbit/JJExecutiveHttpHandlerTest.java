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
package jj.webbit;

import static org.mockito.BDDMockito.*;

import java.net.InetSocketAddress;

import jj.DateFormatHelper;
import jj.ExecutionTrace;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.webbitserver.stub.StubHttpControl;
import org.webbitserver.stub.StubHttpRequest;
import org.webbitserver.stub.StubHttpResponse;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class JJExecutiveHttpHandlerTest {
	
	private static final InetSocketAddress REMOTE_ADDRESS = new InetSocketAddress(80);
	private static final String URI = "/slick!";
	private static final String LENGTH = "3493459";
	private static final int STATUS = 404;
	private static final String REFERRER = "slappy!";
	private static final String USER_AGENT = "reticules";
	
	@Mock Logger logger;
	@Mock ExecutionTrace trace;

	@Test
	public void test() throws Exception {
		
		// given
		JJExecutiveHttpHandler h = new JJExecutiveHttpHandler(logger, trace);
		willReturn(true).given(logger).isTraceEnabled();
		
		StubHttpRequest request = new StubHttpRequest();
		request.header(HttpHeaders.Names.USER_AGENT, USER_AGENT);
		request.header(HttpHeaders.Names.REFERER, REFERRER);
		request.remoteAddress(REMOTE_ADDRESS);
		request.method(HttpMethod.GET.toString());
		request.uri(URI);
		
		StubHttpResponse response = new StubHttpResponse();
		response.status(STATUS);
		
		// when
		// this is a little weird but it ensures agreement on the date
		String date1 = null;
		String date2 = null;
		
		while (date1 == null || !date1.equals(date2)) {
			date1 = DateFormatHelper.nowInAccessLogFormat();
			StubHttpControl control = new StubHttpControl() {
				public void nextHandler(org.webbitserver.HttpRequest request, org.webbitserver.HttpResponse response, org.webbitserver.HttpControl control) {
					response.header(HttpHeaders.Names.CONTENT_LENGTH, LENGTH);
					response.end();
				};
			};
			h.handleHttpRequest(request, response, control);
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
