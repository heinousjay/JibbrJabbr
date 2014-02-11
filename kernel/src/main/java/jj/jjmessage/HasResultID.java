package jj.jjmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

/** extended by message types that expect a result */
class HasResultID {

	@JsonProperty
	public String id;
}
