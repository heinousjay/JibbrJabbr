package jj.jqmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Get extends ExpectsResult {
	
	Get() {}
	
	@JsonProperty
	public String selector;

	@JsonProperty
	public String type;
}
