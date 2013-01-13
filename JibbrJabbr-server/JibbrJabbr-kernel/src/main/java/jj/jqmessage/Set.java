package jj.jqmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Set {
	
	Set() {}
	
	/**
	 * The selector used to get a jquery object
	 */
	@JsonProperty
	public String selector;

	/**
	 * the method to call on the jquery object
	 */
	@JsonProperty
	public String type;
	
	/**
	 * optional - the name to set, ie. attr('name', 'value');
	 */
	@JsonProperty
	public String name;
	
	/**
	 * the value to set
	 */
	@JsonProperty
	public String value;
}
