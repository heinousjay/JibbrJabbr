package jj.engine;

/**
 * enumerates the event handlers set up by the host
 */
public enum HostEvent {
	/** called when a client connects */
	clientConnected,
	/** called when a client disconnects */
	clientDisconnected,
	/** called when the script is being terminated */
	terminating;
}
