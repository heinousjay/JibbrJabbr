package jj.event;

import jj.execution.ServerTask;
import jj.execution.TaskRunner;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;

/**
 * Starts a server task to clean up event listener instances as they
 * are collected
 * Created by jasonmiller on 4/2/16.
 */
@Singleton
class ListenerReferenceCleaner {

	@Inject
	ListenerReferenceCleaner(EventSystemState state, TaskRunner taskRunner) {
		taskRunner.execute(new ServerTask("Event System cleanup") {

			@Override
			protected void run() throws Exception {
				//noinspection InfiniteLoopStatement
				while (true) {
					Reference<?> reference = state.referenceQueue.remove();
					state.instancesByReceiverType.values().forEach(
						instances -> {
							for (Iterator<WeakReference<Object>> i = instances.iterator(); i.hasNext();) {
								if (i.next() == reference) {
									i.remove();
								}
							}
						}
					);
				}
			}
		});
	}


}
