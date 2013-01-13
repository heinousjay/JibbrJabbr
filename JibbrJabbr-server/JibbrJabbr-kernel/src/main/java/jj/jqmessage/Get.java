package jj.jqmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Get extends ExpectsResult {
	
	Get() {}
	
	/**
	 * the selector used to get a jquery object
	 */
	@JsonProperty
	public String selector;

	/**
	 * the method to invoke
	 */
	@JsonProperty
	public String type;
	
	/**
	 * optional, the name to retrieve, ie. attr('checked')
	 */
	@JsonProperty
	public String name;
}
