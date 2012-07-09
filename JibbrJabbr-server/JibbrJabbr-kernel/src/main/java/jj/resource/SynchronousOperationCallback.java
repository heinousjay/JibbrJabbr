package jj.resource;

import jj.AsyncThreadPool;

/**
 * <p>
 * Certain operations in the kernel which would normally block the calling thread
 * are instead delegated to a special thread pool for handling these blocking
 * operations, and upon completion, this callback is invoked.
 * </p>
 * 
 * <p>
 * This structure should be used for any operation that may block for any reason, such
 * as querying the file system, acquiring a lock, sleeping the thread, etc.
 * </p>
 * 
 * <p>
 * If there is no reasonable success parameter, then the operation can be parameterized
 * with Void
 * <p>
 * 
 * <p>
 * this is more or less a substitution for a CompletionHandler and may at some point
 * in the future be refactored into one
 * </p>
 * 
 * @param T The type of the value available upon success
 * 
 * @author jason
 *
 */
public abstract class SynchronousOperationCallback<T> {
	
	/**
	 * Define this method to return true if you need this callback to be invoked synchronously,
	 * or leave it as is to be invoked asynchronously.
	 * 
	 * Only change this if absolutely necessary for your flow to work correctly.
	 * @return
	 */
	public boolean invokeSynchronously() {
		return false;
	}
	
	/**
	 * Called upon non-exceptional completion of the operation.
	 * @param parameter a parameter as defined by the specific operation
	 */
	public void complete(final T parameter) {}
	
	/**
	 * Called if the operation throws
	 * @param e a {@link Throwable} describing what happened.
	 */
	public void throwable(final Throwable t) {}
	
	public final void invokeComplete(final AsyncThreadPool asyncExecutor, final T parameter) {
		if (invokeSynchronously()) {
			complete(parameter);
		} else {
			asyncExecutor.execute(completionInvoker(parameter));
		}
	}
	
	public final Runnable completionInvoker(final T parameter) {
		return new Runnable() {
			@Override
			public void run() {
				complete(parameter);
			}
		};
	}
	
	public final void invokeThrowable(final AsyncThreadPool asyncExecutor, final Throwable t) {
		if (invokeSynchronously()) {
			throwable(t);
		} else {
			asyncExecutor.execute(throwableInvoker(t));
		}
	}
	
	public final Runnable throwableInvoker(final Throwable t) {
		return new Runnable() {
			@Override
			public void run() {
				throwable(t);
			}
		};
	}
}
