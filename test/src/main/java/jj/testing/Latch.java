/*
 *    Copyright 2016 Jason Miller
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
package jj.testing;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Integration test latch that waits additional time when running on travis CI
 * @author Jason Miller
 */
public class Latch {

	private final CountDownLatch latch;

	public Latch(int count) {
		latch = new CountDownLatch(count);
	}

	public void countDown() {
		latch.countDown();
	}

	public long getCount() {
		return latch.getCount();
	}

	public void await(long timeout, TimeUnit unit) throws InterruptedException {
		long timeoutMillis = TimeUnit.MILLISECONDS.convert(timeout, unit);
		timeoutMillis += "true".equals(System.getenv("CI")) ? 2000 : 0;
		if (!latch.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
			throw new AssertionError("timed out in " + timeoutMillis + " ms");
		}
	}
}
