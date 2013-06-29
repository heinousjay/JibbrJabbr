package jj.http;

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

/**
 * Acts as the bridge from netty into our core.
 * @author jason
 *
 */
@Singleton
public class JJEngineHttpHandler {
	
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
	
	private Servable[] findMatchingServables(final JJHttpRequest request) {
		
		List<Servable> result = new ArrayList<>();
		
		for (final Servable type : resourceTypes) {
			if (type.isMatchingRequest(request)) {
				result.add(type);
			}
		}
		
		return result.toArray(new Servable[result.size()]);
	}

	@HttpControlThread
	public void handleHttpRequest(
		final JJHttpRequest request,
		final JJHttpResponse response
	) throws Exception {
		// figure out if there's something for us to do
		final Servable[] servables = findMatchingServables(request);
		
		if (servables.length > 0) {
			dispatchNextServable(request, response, servables, new AtomicInteger());
			
		} else {
			nextHandler(response);
		}
	}
	
	private void dispatchNextServable(
		final JJHttpRequest request,
		final JJHttpResponse response,
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
							response
						);
					
					if (requestProcessor != null) {
						requestProcessor.process();
					} else if (count.get() < servables.length) {
						dispatchNextServable(request, response, servables, count);
					} else {
						nextHandler(response);
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
	
	private void nextHandler(final JJHttpResponse response) {
		Runnable r = executors.prepareTask(new JJRunnable("HtmlEngine passing control to next handler") {
			
			@Override
			@HttpControlThread
			public void run() throws Exception {
				try {
					
					//control.nextHandler();
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
