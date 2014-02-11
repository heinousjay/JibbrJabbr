package jj.jjmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

/** extended by message types that transmit a result */
class HasResultID {

	@JsonProperty
	public String id;
}
