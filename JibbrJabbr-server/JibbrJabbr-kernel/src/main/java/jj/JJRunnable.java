package jj;

/**
 * The way to run a task
 * 
 * @author jason
 *
 */
public interface JJRunnable {
	
	String name();

	void run() throws Exception;

}
