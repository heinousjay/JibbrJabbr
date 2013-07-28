package jj.servable;

import java.io.IOException;

/**
 * returned by a Servable if the request can be handled
 * @author jason
 *
 */
public interface RequestProcessor {
	
	/**
	 * Called to perform the processing
	 * required.
	 */
	void process() throws IOException;
	
	
}