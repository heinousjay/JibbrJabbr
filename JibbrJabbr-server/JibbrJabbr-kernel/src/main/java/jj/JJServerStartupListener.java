package jj;

/**
 * implement to receive a notification of server start up
 * 
 * @author jason
 *
 */
public interface JJServerStartupListener {
	
	public enum Priority {
		Highest,
		NearHighest,
		Middle,
		NearLowest,
		Lowest
	}

	void start() throws Exception;
	
	Priority startPriority();
}
