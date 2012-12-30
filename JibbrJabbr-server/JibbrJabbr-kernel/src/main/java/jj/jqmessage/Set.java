package jj.jqmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Set {
	
	Set() {}
	
	@JsonProperty
	public String selector;

	@JsonProperty
	public String type;
	
	@JsonProperty
	public String value;
}
