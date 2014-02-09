package jj.engine;

/**
 * enumerates the event handlers set up by the host
 */
public enum HostEvent {
	/** called when a client connects */
	clientConnected,
	/** called when a client disconnects */
	clientDisconnected,
	/** called when there is an error */
	clientErrored,
	/** called when the script is being terminated */
	terminating;
}
