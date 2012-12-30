package jj.webbit;

import java.util.Arrays;

import jj.HttpControlThread;
import jj.IOThread;
import jj.JJExecutors;
import jj.JJRunnable;
import jj.servable.Servable;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

/**
 * Acts as the bridge from webbit into our core.
 * @author jason
 *
 */
class JJEngineHttpHandler implements HttpHandler {
	
	private final JJExecutors executors;
	
	private final Servable[] resourceTypes;
	
	JJEngineHttpHandler( 
		final JJExecutors executors,
		final Servable[] resourceTypes
	) {
		this.executors = executors;
		this.resourceTypes = resourceTypes;
		Arrays.sort(this.resourceTypes);
	}
	
	private Servable findMatchingServable(final JJHttpRequest request) {
		
		Servable result = null;
		
		for (final Servable type : resourceTypes) {
			if (type.isMatchingRequest(request)) {
				result = type;
				break;
			}
		}
		
		return result;
	}

	@Override
	@HttpControlThread
	public void handleHttpRequest(
		final HttpRequest request,
		final HttpResponse response,
		final HttpControl control
	) throws Exception {
		final JJHttpRequest jjrequest = new JJHttpRequest(request);
		
		// figure out if there's something for us to do
		final Servable servable = findMatchingServable(jjrequest);
		
		if (servable != null) {
			JJRunnable r = new JJRunnable("JJEngine webbit->core processing") {
				
				@Override
				public void innerRun() throws Exception {
					try {
						RequestProcessor requestProcessor = 
							servable.makeRequestProcessor(
								jjrequest,
								response,
								control
							);
						
						if (requestProcessor != null) {
							requestProcessor.process();
						} else {
							nextHandler(control);
						}
						
					} catch (Exception e) {
						nextHandler(control);
						throw e;
					}
				}
			};
			
			if (servable.needsIO(jjrequest)) {
				executors.ioExecutor().execute(r);
			} else {
				r.run();
			}
			
		} else {
			nextHandler(control);
		}
	}
	
	@IOThread
	private void nextHandler(final HttpControl control) {
		if (executors.isIOThread()) {
			control.execute(new JJRunnable("HtmlEngine passing control to next handler") {
	
				@Override
				@HttpControlThread
				protected void innerRun() throws Exception {
					control.nextHandler();
				}
				
			});
		} else {
			control.nextHandler();
		}
	}
	
}
