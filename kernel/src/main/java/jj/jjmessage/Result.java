package jj.jjmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Result extends HasResultID {

	Result() {}
	
	@JsonProperty
	public String value;
}
