package jj.hostapi;

import org.jsoup.nodes.Element;
import org.mozilla.javascript.Callable;

/**
 * Interface to ensure 
 * @author jason
 *
 */
public interface Selection {
	
	/**
	 * The selector that created this selection
	 * @return
	 */
	String selector();
	
	// -- Events
	Selection on(final String type, final Callable function);
	
	Selection on(final String type, final String selector, final Callable function);
	
	/**
	 * attaches an event handler to the named event on all matched elements
	 * @param type
	 * @param function
	 * @return
	 */
	Selection bind(String type, Callable function);
	
	/**
	 * attaches an event handler to the named event on all matched elements, passing an optional piece of data
	 * @param type
	 * @param data
	 * @param function
	 * @return
	 */
	Selection bind(String type, Object data, Callable function);
	
	/**
	 * binds an event handler that prevents default and cancels bubble on all matched elements
	 * @param type
	 * @param cancel
	 * @return
	 */
	Selection bind(String type, boolean cancel);
	
	/**
	 * binds an event handler that prevents default and cancels bubble on all matched elements.
	 * event data is effectively ignored, this method is declared to be complete
	 * @param type
	 * @param cancel
	 * @return
	 */
	Selection bind(String type, Object data, boolean cancel);
	
	/**
	 * attaches a click event to all of the matched elements
	 */
	Selection click(Callable function);
	
	/**
	 * attaches an event to all of the matched elements specifically listening for the enter key
	 * should i name this 'activate' instead?
	 * @param function
	 * @return
	 */
	Selection enter(Callable function);

	/**
	 * Sets the given data value under the given data key to all matched elements
	 * @param key
	 * @param value
	 * @return
	 */
	Selection data(final String key, final String value);
	
	/**
	 * Set the text of the matched elements. Any existing contents (text or elements) will be cleared.
	 * @param text
	 * @return
	 */
	Selection text(String text);
	
	/**
	 * appends all of the matched elements in the input selection to this selection.
	 * The first element in this list will get the original nodes.  All subsequent
	 * elements will receive clones.
	 * @param selection
	 * @return
	 */
	Selection append(final Selection selection);
	
	/// --------------------
	// interface copied from org.jsoup.nodes.Element
	
	// attribute methods
	/**
	 Get an attribute value from the first matched element that has the attribute.
	 @param attributeKey The attribute key.
	 @return The attribute value from the first matched element that has the attribute.. If no elements were matched (isEmpty() == true),
	 or if the no elements have the attribute, returns empty string.
	 @see #hasAttr(String)
	 */
	String attr(String attributeKey);

	/**
	 Checks if any of the matched elements have this attribute set.
	 @param attributeKey attribute key
	 @return true if any of the elements have the attribute; false if none do.
	 */
	boolean hasAttr(String attributeKey);

	/**
	 * Set an attribute on all matched elements.
	 * @param attributeKey attribute key
	 * @param attributeValue attribute value
	 * @return this
	 */
	Selection attr(String attributeKey, String attributeValue);

	/**
	 * Remove an attribute from every matched element.
	 * @param attributeKey The attribute to remove.
	 * @return this (for chaining)
	 */
	Selection removeAttr(String attributeKey);

	/**
	 Add the class name to every matched element's {@code class} attribute.
	 @param className class name to add
	 @return this
	 */
	Selection addClass(String className);

	/**
	 Remove the class name from every matched element's {@code class} attribute, if present.
	 @param className class name to remove
	 @return this
	 */
	Selection removeClass(String className);

	/**
	 Toggle the class name on every matched element's {@code class} attribute.
	 @param className class name to add if missing, or remove if present, from every element.
	 @return this
	 */
	Selection toggleClass(String className);

	/**
	 Determine if any of the matched elements have this class name set in their {@code class} attribute.
	 @param className class name to check for
	 @return true if any do, false if none do
	 */
	boolean hasClass(String className);

	/**
	 * Get the form element's value of the first matched element.
	 * @return The form element's value, or empty if not set.
	 * @see Element#val()
	 */
	String val();

	/**
	 * Set the form element's value in each of the matched elements.
	 * @param value The value to set into each matched element
	 * @return this (for chaining)
	 */
	Selection val(String value);

	/**
	 * Get the combined text of all the matched elements.
	 * <p>
	 * Note that it is possible to get repeats if the matched elements contain both parent elements and their own
	 * children, as the Element.text() method returns the combined text of a parent and all its children.
	 * @return string of all text: unescaped and no HTML.
	 * @see Element#text()
	 */
	String text();

	/**
	 * Get the combined inner HTML of all matched elements.
	 * @return string of all element's inner HTML.
	 * @see #text()
	 * @see #outerHtml()
	 */
	String html();

	/**
	 * Set the inner HTML of each matched element.
	 * @param html HTML to parse and set into each matched element.
	 * @return this, for chaining
	 * @see Element#html(String)
	 */
	Selection html(String html);

	/**
	 * Add the supplied HTML to the start of each matched element's inner HTML.
	 * @param html HTML to add inside each element, before the existing HTML
	 * @return this, for chaining
	 * @see Element#prepend(String)
	 */
	Selection prepend(String html);

	/**
	 * Add the supplied HTML to the end of each matched element's inner HTML.
	 * @param html HTML to add inside each element, after the existing HTML
	 * @return this, for chaining
	 * @see Element#append(String)
	 */
	Selection append(String html);

	/**
	 * Insert the supplied HTML before each matched element's outer HTML.
	 * @param html HTML to insert before each element
	 * @return this, for chaining
	 * @see Element#before(String)
	 */
	Selection before(String html);

	/**
	 * Insert the supplied HTML after each matched element's outer HTML.
	 * @param html HTML to insert after each element
	 * @return this, for chaining
	 * @see Element#after(String)
	 */
	Selection after(String html);

	/**
	 Wrap the supplied HTML around each matched elements. For example, with HTML
	 {@code <p><b>This</b> is <b>Jsoup</b></p>},
	 <code>doc.select("b").wrap("&lt;i&gt;&lt;/i&gt;");</code>
	 becomes {@code <p><i><b>This</b></i> is <i><b>jsoup</b></i></p>}
	 @param html HTML to wrap around each element, e.g. {@code <div class="head"></div>}. Can be arbitrarily deep.
	 @return this (for chaining)
	 @see Element#wrap
	 */
	Selection wrap(String html);

	/**
	 * Removes the matched elements from the DOM, and moves their children up into their parents. This has the effect of
	 * dropping the elements but keeping their children.
	 * <p/>
	 * This is useful for e.g removing unwanted formatting elements but keeping their contents.
	 * <p/>
	 * E.g. with HTML: {@code <div><font>One</font> <font><a href="/">Two</a></font></div>}<br/>
	 * {@code doc.select("font").unwrap();}<br/>
	 * HTML = {@code <div>One <a href="/">Two</a></div>}
	 *
	 * @return this (for chaining)
	 */
	Selection unwrap();

    /**
     * Empty (remove all child nodes from) each matched element. This is similar to setting the inner HTML of each
     * element to nothing.
     * <p>
     * E.g. HTML: {@code <div><p>Hello <b>there</b></p> <p>now</p></div>}<br>
     * <code>doc.select("p").empty();</code><br>
     * HTML = {@code <div><p></p> <p></p></div>}
     * @return this, for chaining
     * @see Element#empty()
     * @see #remove()
     */
	Selection empty();

	/**
	 * Remove each matched element from the DOM. This is similar to setting the outer HTML of each element to nothing.
	 * <p>
	 * E.g. HTML: {@code <div><p>Hello</p> <p>there</p> <img /></div>}<br>
	 * <code>doc.select("p").remove();</code><br>
	 * HTML = {@code <div> <img /></div>}
	 * <p>
	 * Note that this method should not be used to clean user-submitted HTML; rather, use {@link org.jsoup.safety.Cleaner} to clean HTML.
	 * @return this, for chaining
	 * @see Element#empty()
	 * @see #empty()
	 */
	Selection remove();

	/**
	 * Find matching elements within this element list.
	 * @param query A {@link Selection} query
	 * @return the filtered list of elements, or an empty list if none match.
	 */
	Selection select(String query);

	/**
	 * Remove elements from this list that match the {@link Selection} query.
	 * <p>
	 * E.g. HTML: {@code <div class=logo>One</div> <div>Two</div>}<br>
	 * <code>Elements divs = doc.select("div").not("#logo");</code><br>
	 * Result: {@code divs: [<div>Two</div>]}
	 * <p>
	 * @param query the selector query whose results should be removed from these elements
	 * @return a new elements list that contains only the filtered results
	 */
	Selection not(String query);

	/**
	 * Get the <i>nth</i> matched element as an Elements object.
	 * <p>
	 * See also {@link #get(int)} to retrieve an Element.
	 * @param index the (zero-based) index of the element in the list to retain
	 * @return Elements containing only the specified element, or, if that element did not exist, an empty list.
	 */
	Selection eq(int index);

	/**
	 * Test if any of the matched elements match the supplied query.
	 * @param query A selector
	 * @return true if at least one element in the list matches the query.
	 */
	boolean is(String query);

	/**
	 * Get all of the parents and ancestor elements of the matched elements.
	 * @return all of the parents and ancestor elements of the matched elements
	 */
	Selection parents();

	// list-like methods
	/**
	 Get the first matched element.
	 @return The first matched element, or <code>null</code> if contents is empty;
	 */
	Element first();

	/**
	 Get the last matched element.
	 @return The last matched element, or <code>null</code> if contents is empty.
	 */
	Element last();

}