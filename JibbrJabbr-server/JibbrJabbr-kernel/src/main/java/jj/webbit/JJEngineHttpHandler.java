package jj.webbit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
	
	private final JJHttpRequestCreator creator;
	
	@Inject
	JJEngineHttpHandler( 
		final JJExecutors executors,
		final JJHttpRequestCreator creator,
		final Set<Servable> resourceTypes
	) {
		this.executors = executors;
		this.creator = creator;
		this.resourceTypes = resourceTypes;
	}
	
	private Servable[] findMatchingServables(final JJHttpRequest request) {
		
		List<Servable> result = new ArrayList<>();
		
		for (final Servable type : resourceTypes) {
			if (type.isMatchingRequest(request)) {
				result.add(type);
			}
		}
		
		return result.toArray(new Servable[result.size()]);
	}

	@Override
	@HttpControlThread
	public void handleHttpRequest(
		final HttpRequest request,
		final HttpResponse response,
		final HttpControl control
	) throws Exception {
		final JJHttpRequest jjrequest = creator.createJJHttpRequest(request);
		
		// figure out if there's something for us to do
		final Servable[] servables = findMatchingServables(jjrequest);
		
		if (servables.length > 0) {
			dispatchNextServable(jjrequest, response, control, servables, new AtomicInteger());
			
		} else {
			nextHandler(control, response);
		}
	}
	
	private void dispatchNextServable(
		final JJHttpRequest request,
		final HttpResponse response,
		final HttpControl control,
		final Servable[] servables,
		final AtomicInteger count
	) {
		Runnable r = executors.prepareTask(new JJRunnable("JJEngine webbit->core processing") {
			
			@Override
			public void run() throws Exception {
				try {
					RequestProcessor requestProcessor = 
						servables[count.getAndIncrement()].makeRequestProcessor(
							request,
							response,
							control
						);
					
					if (requestProcessor != null) {
						requestProcessor.process();
					} else if (count.get() < servables.length) {
						dispatchNextServable(request, response, control, servables, count);
					} else {
						nextHandler(control, response);
					}
					
				} catch (Throwable e) {
					response.error(e);
				}
			}
		});
		
		if (servables[0].needsIO(request)) {
			executors.ioExecutor().execute(r);
		} else {
			executors.httpControlExecutor().execute(r);
		}
	}
	
	private void nextHandler(final HttpControl control, final HttpResponse response) {
		Runnable r = executors.prepareTask(new JJRunnable("HtmlEngine passing control to next handler") {
			
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
		if (executors.isHttpControlThread()) {
			r.run();
		} else {
			executors.httpControlExecutor().execute(r);
		}
	}
	
}
