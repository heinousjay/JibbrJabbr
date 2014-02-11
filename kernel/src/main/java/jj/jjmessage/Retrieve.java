package jj.jjmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Retrieval request
 * @author jason
 *
 */
public class Retrieve extends HasResultID {

	@JsonProperty
	public String key;
}
