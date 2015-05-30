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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.List;

import jj.http.server.RangeHeaderReader.Range;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class RangeHeaderReaderTest {
	
	HttpHeaders requestHeaders;
	
	RangeHeaderReader rangeHeaderReader(String headerValue, long responseSize, long overlapDistance) {
		requestHeaders = new DefaultHttpHeaders();
		requestHeaders.set(HttpHeaderNames.RANGE, headerValue);
		return new RangeHeaderReader(requestHeaders, responseSize, overlapDistance);
	}

	@Test
	public void test1() {
		
		List<Range> ranges = rangeHeaderReader("bytes=0-100,200-399", 200L, 80).ranges();
		
		assertThat(ranges.size(), is(1));
		assertThat(ranges.get(0).start, is(0L));
		assertThat(ranges.get(0).end, is(100L));
	}
	
	@Test
	public void test2() {
		List<Range> ranges = rangeHeaderReader("bytes=0-100,200-399", 400L, 80).ranges();
		
		assertThat(ranges.size(), is(2));
		assertThat(ranges.get(0).start, is(0L));
		assertThat(ranges.get(0).end, is(100L));
		assertThat(ranges.get(1).start, is(200L));
		assertThat(ranges.get(1).end, is(399L));
	}
	
	@Test
	public void test2a() {
		List<Range> ranges = rangeHeaderReader("bytes=0-100,200-399", 400L, 100).ranges();
		
		assertThat(ranges.size(), is(2));
		assertThat(ranges.get(0).start, is(0L));
		assertThat(ranges.get(0).end, is(100L));
		assertThat(ranges.get(1).start, is(200L));
		assertThat(ranges.get(1).end, is(399L));
	}
	
	@Test
	public void test3() {
		List<Range> ranges = rangeHeaderReader("bytes=0-100,170-399", 400L, 80).ranges();
		
		assertThat(ranges.size(), is(1));
		assertThat(ranges.get(0).start, is(0L));
		assertThat(ranges.get(0).end, is(399L));
	}
	
	@Test
	public void test4() {
		
		RangeHeaderReader rangeHandler = rangeHeaderReader("bytes=-100,-200,1-199,2-198,-198,500-", 2001L, 80);
		assertThat(rangeHandler.ranges().size(), is(0));
		assertThat(rangeHandler.isBadRequest(), is(true));
	}
	
	@Test
	public void test5() {
		requestHeaders = new DefaultHttpHeaders();
		requestHeaders.set(HttpHeaderNames.RANGE, "bytes=-100,-200,1-199,2-198,-198,500-");
		List<Range> ranges = new RangeHeaderReader(requestHeaders, 2001L, 80, 6).ranges();
		
		assertThat(ranges.size(), is(2));
		assertThat(ranges.get(0).start, is(1L));
		assertThat(ranges.get(0).end, is(199L));
		assertThat(ranges.get(1).start, is(500L));
		assertThat(ranges.get(1).end, is(2000L));
	}

}
