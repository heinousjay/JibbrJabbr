package jj.webbit;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.HttpControlThread;
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
@Singleton
public class JJEngineHttpHandler implements HttpHandler {
	
	private final JJExecutors executors;
	
	private final Set<Servable> resourceTypes;
	
	@Inject
	JJEngineHttpHandler( 
		final JJExecutors executors,
		final Set<Servable> resourceTypes
	) {
		this.executors = executors;
		this.resourceTypes = resourceTypes;
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
			Runnable r = executors.prepareTask(new JJRunnable() {
				
				@Override
				public String name() {
					return "JJEngine webbit->core processing";
				}

				@Override
				public void run() throws Exception {
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
							nextHandler(control, response);
						}
						
					} catch (Throwable e) {
						response.error(e);
					}
				}
			});
			
			if (servable.needsIO(jjrequest)) {
				executors.ioExecutor().execute(r);
			} else {
				r.run();
			}
			
		} else {
			nextHandler(control, response);
		}
	}
	
	private void nextHandler(final HttpControl control, final HttpResponse response) {
		Runnable r = executors.prepareTask(new JJRunnable() {

			@Override
			public String name() {
				return "HtmlEngine passing control to next handler";
			}

			@Override
			@HttpControlThread
			public void run() throws Exception {
				try {
					control.nextHandler();
				} catch (Throwable t) {
					response.error(t);
				}
			}
			
		});
		if (executors.isIOThread()) {
			executors.httpControlExecutor().execute(r);
		} else {
			r.run();
		}
	}
	
}
