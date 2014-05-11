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
package jj.util;

import java.util.concurrent.TimeUnit;

import jj.util.Clock;

/**
 * @author jason
 *
 */
public class MockClock extends Clock {

	public long time = System.currentTimeMillis();
	
	@Override
	public long time() {
		return time;
	}
	
	public MockClock advance() {
		time++;
		return this;
	}
	
	public MockClock advance(long time, TimeUnit timeUnit) {
		this.time += TimeUnit.MILLISECONDS.convert(time, timeUnit);
		return this;
	}
	
	public MockClock retreat() {
		time--;
		return this;
	}
	
	public MockClock retreat(long time, TimeUnit timeUnit) {
		this.time -= TimeUnit.MILLISECONDS.convert(time, timeUnit);
		return this;
	}
}
