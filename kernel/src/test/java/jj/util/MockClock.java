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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

/**
 * @author jason
 *
 */
public class MockClock extends Clock {

	public long time = System.currentTimeMillis();
	
	public MockClock advance() {
		time++;
		return this;
	}
	
	public MockClock advance(long time, TimeUnit timeUnit) {
		this.time += TimeUnit.MILLISECONDS.convert(time, timeUnit);
		return this;
	}

	@Override
	public ZoneId getZone() {
		return ZoneId.systemDefault();
	}

	@Override
	public Clock withZone(ZoneId zone) {
		return null; // not used in the system so we don't really bother, yet
	}

	@Override
	public long millis() {
		return time;
	}

	@Override
	public Instant instant() {
		return Instant.ofEpochMilli(time);
	}
}
