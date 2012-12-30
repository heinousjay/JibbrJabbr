package jj.jqmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * identifies an element for communication purposes
 * @author jason
 *
 */
public class Element {

	Element() {}
	
	@JsonProperty
	public String selector;
}
