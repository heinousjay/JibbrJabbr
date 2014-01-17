package jj.jjmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * Message from server to client to append an element
 * set identified by the selector passed in child to the
 * element set identified by the selector passed in parent.
 * </p>
 * <p>
 * No response expected.
 * </p>
 * @author jason
 *
 */
public class Append {

	@JsonProperty
	public String parent;
	
	@JsonProperty
	public String child;
}
