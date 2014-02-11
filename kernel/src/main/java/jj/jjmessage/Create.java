package jj.jjmessage;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Create extends HasResultID {

	Create() {}
	
	@JsonProperty
	public String html;
	
	@JsonProperty
	public Map<?, ?> args;
}
