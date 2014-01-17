package jj.jjmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

/** extended by message types that expect a result */
public class ExpectsResult {

	@JsonProperty
	public String id;
}
