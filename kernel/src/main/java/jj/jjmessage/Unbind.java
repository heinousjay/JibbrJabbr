package jj.jjmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A message that communicates information about an event-element unbinding
 * in the style of $.fn.off (see <a href="http://api.jquery.com/off/">http://api.jquery.com/off/</a>)
 * @author jason
 *
 */
public class Unbind {
	
	Unbind() {}
	
	/**
	 * The context of the unbinding. if not specified, will
	 * default to the document.
	 */
	@JsonProperty
	public String context;

	/**
	 * The type of event we are unbinding.  can be a
	 * jj-internal "special" event type, like enter.
	 */
	@JsonProperty
	public String type;
	
	/**
	 * The selector to use for unbinding the events
	 */
	@JsonProperty
	public String selector;
	
}
