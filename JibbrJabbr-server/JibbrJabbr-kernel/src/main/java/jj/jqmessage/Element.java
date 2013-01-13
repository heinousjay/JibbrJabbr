package jj.jqmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * returning an element from the client.  it is
 * expected that the selector will be an id selector
 * @author jason
 *
 */
public class Element {

	Element() {}
	
	@JsonProperty
	public String id;
	
	@JsonProperty
	public String selector;
}
