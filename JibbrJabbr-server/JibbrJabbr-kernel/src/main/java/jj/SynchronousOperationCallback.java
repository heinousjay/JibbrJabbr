package jj;

/**
 * <p>
 * Certain operations in the kernel which would normally block the calling thread
 * are instead delegated to a special thread pool for handling these blocking
 * operations, and upon completion, this callback is invoked in the KernelThreadPool
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
 * @author jason
 *
 */
public abstract class SynchronousOperationCallback<T> {
	
	/**
	 * Called upon success of the operation.
	 * @param parameter a parameter as defined by the specific operation
	 */
	public void success(T parameter) {}
	
	/**
	 * Called up failure of the operation.
	 * @param e a Throwable describing the failure, if available. Might be null.
	 */
	public void failure(Throwable e) {}
}
