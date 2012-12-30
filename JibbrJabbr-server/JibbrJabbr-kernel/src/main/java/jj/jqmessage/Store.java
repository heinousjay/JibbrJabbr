package jj.jqmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * storage request
 * 
 * @author jason
 *
 */
public class Store {

	@JsonProperty
	public String key;
	
	@JsonProperty
	public String value;
}
