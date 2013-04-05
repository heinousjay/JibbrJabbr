package jj;

/**
 * implement to receive a notification of server start up
 * 
 * @author jason
 *
 */
public interface JJServerListener {

	void start() throws Exception;
	
	void stop();
}
