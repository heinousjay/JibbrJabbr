package jj.jjmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Invoke extends ExpectsResult {

	@JsonProperty
	public String name;
	
	/**
	 * Should be a JSON string of the arguments to the original function
	 */
	@JsonProperty
	public String args;
}
