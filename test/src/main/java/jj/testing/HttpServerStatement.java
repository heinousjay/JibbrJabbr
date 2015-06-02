package jj.testing;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.CountDownLatch;

import javax.inject.Singleton;

import jj.event.Listener;
import jj.event.Subscriber;
import jj.http.server.HttpServerStarted;
import jj.http.server.HttpServerStopped;

@Singleton
@Subscriber
class HttpServerStatement extends JibbrJabbrTestStatement {

	private final CountDownLatch startLatch = new CountDownLatch(1);
	private final CountDownLatch stopLatch = new CountDownLatch(1);
	
	@Listener
	void on(HttpServerStarted event) {
		startLatch.countDown();
	}
	
	@Listener
	void on(HttpServerStopped event) {
		stopLatch.countDown();
	}
	
	@Override
	public void evaluate() throws Throwable {
		boolean success = startLatch.await(500, MILLISECONDS);
		if (!success) {
			throw new AssertionError("http server did not start in 500 milliseconds");
		}
		
		evaluateInner();
		
		stopLatch.await(500, MILLISECONDS);
		
	}

}
