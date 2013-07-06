package jj.jqmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Event {

	Event() {}
	
	@JsonProperty
	public String context;
	
	@JsonProperty
	public String selector;
	
	@JsonProperty
	public String type;
	
	@JsonProperty
	public int which;
	
	@JsonProperty
	public String target;
	
	@JsonProperty
	public String form;
}
