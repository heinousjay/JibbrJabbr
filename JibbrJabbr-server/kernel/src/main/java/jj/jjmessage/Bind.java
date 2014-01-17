package jj.jjmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A message that communicates information about an event-element binding
 * in the style of $.fn.on (see http://api.jquery.com/on/)
 * @author jason
 *
 */
public class Bind {
	
	Bind() {}
	
	/**
	 * The context of the binding. if not specified, will
	 * default to the document.
	 */
	@JsonProperty
	public String context;

	/**
	 * The type of event we are binding.  can be a
	 * jj-internal "special" event type, like enter.
	 */
	@JsonProperty
	public String type;
	
	/**
	 * The selector to use for binding the events
	 */
	@JsonProperty
	public String selector;
	
}
