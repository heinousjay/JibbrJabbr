package jj.jqmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Result {

	Result() {}
	
	@JsonProperty
	public String id;
	
	@JsonProperty
	public String value;
}
