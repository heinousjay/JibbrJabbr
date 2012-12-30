package jj.jqmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Event extends Element {

	Event() {}
	
	@JsonProperty
	public String type;
	
	@JsonProperty
	public int which;
}
