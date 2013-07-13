package jj.engine;

/**
 * enumerates the event handlers set up by the host
 * system.  just list it here, and it shall be done
 */
public enum HostEvent {
	/** called when a client connects */
	clientConnected,
	/** called when a client disconnects */
	clientDisconnected,
	/** called when the script is being terminated */
	terminating;
}
