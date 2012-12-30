package jj.jqmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Retrieval request
 * @author jason
 *
 */
public class Retrieve extends ExpectsResult {

	@JsonProperty
	public String key;
}
