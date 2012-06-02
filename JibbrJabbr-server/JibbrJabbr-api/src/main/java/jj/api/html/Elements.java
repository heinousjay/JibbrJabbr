package jj.api.html;

import java.util.List;

public interface Elements extends List<Element>, Cloneable {

	Elements clone();

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
	Elements attr(String attributeKey, String attributeValue);

	/**
	 * Remove an attribute from every matched element.
	 * @param attributeKey The attribute to remove.
	 * @return this (for chaining)
	 */
	Elements removeAttr(String attributeKey);

	/**
	 Add the class name to every matched element's {@code class} attribute.
	 @param className class name to add
	 @return this
	 */
	Elements addClass(String className);

	/**
	 Remove the class name from every matched element's {@code class} attribute, if present.
	 @param className class name to remove
	 @return this
	 */
	Elements removeClass(String className);

	/**
	 Toggle the class name on every matched element's {@code class} attribute.
	 @param className class name to add if missing, or remove if present, from every element.
	 @return this
	 */
	Elements toggleClass(String className);

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
	Elements val(String value);

	/**
	 * Get the combined text of all the matched elements.
	 * <p>
	 * Note that it is possible to get repeats if the matched elements contain both parent elements and their own
	 * children, as the Element.text() method returns the combined text of a parent and all its children.
	 * @return string of all text: unescaped and no HTML.
	 * @see Element#text()
	 */
	String text();

	boolean hasText();

	/**
	 * Get the combined inner HTML of all matched elements.
	 * @return string of all element's inner HTML.
	 * @see #text()
	 * @see #outerHtml()
	 */
	String html();

	/**
	 * Get the combined outer HTML of all matched elements.
	 * @return string of all element's outer HTML.
	 * @see #text()
	 * @see #html()
	 */
	String outerHtml();

	/**
	 * Get the combined outer HTML of all matched elements. Alias of {@link #outerHtml()}.
	 * @return string of all element's outer HTML.
	 * @see #text()
	 * @see #html()
	 */
	String toString();

	/**
	 * Update the tag name of each matched element. For example, to change each {@code <i>} to a {@code <em>}, do
	 * {@code doc.select("i").tagName("em");}
	 * @param tagName the new tag name
	 * @return this, for chaining
	 * @see Element#tagName(String)
	 */
	Elements tagName(String tagName);

	/**
	 * Set the inner HTML of each matched element.
	 * @param html HTML to parse and set into each matched element.
	 * @return this, for chaining
	 * @see Element#html(String)
	 */
	Elements html(String html);

	/**
	 * Add the supplied HTML to the start of each matched element's inner HTML.
	 * @param html HTML to add inside each element, before the existing HTML
	 * @return this, for chaining
	 * @see Element#prepend(String)
	 */
	Elements prepend(String html);

	/**
	 * Add the supplied HTML to the end of each matched element's inner HTML.
	 * @param html HTML to add inside each element, after the existing HTML
	 * @return this, for chaining
	 * @see Element#append(String)
	 */
	Elements append(String html);

	/**
	 * Insert the supplied HTML before each matched element's outer HTML.
	 * @param html HTML to insert before each element
	 * @return this, for chaining
	 * @see Element#before(String)
	 */
	Elements before(String html);

	/**
	 * Insert the supplied HTML after each matched element's outer HTML.
	 * @param html HTML to insert after each element
	 * @return this, for chaining
	 * @see Element#after(String)
	 */
	Elements after(String html);

	/**
	 Wrap the supplied HTML around each matched elements. For example, with HTML
	 {@code <p><b>This</b> is <b>Jsoup</b></p>},
	 <code>doc.select("b").wrap("&lt;i&gt;&lt;/i&gt;");</code>
	 becomes {@code <p><i><b>This</b></i> is <i><b>jsoup</b></i></p>}
	 @param html HTML to wrap around each element, e.g. {@code <div class="head"></div>}. Can be arbitrarily deep.
	 @return this (for chaining)
	 @see Element#wrap
	 */
	Elements wrap(String html);

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
	 * @see Node#unwrap
	 */
	Elements unwrap();

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
	Elements empty();

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
	Elements remove();

	/**
	 * Find matching elements within this element list.
	 * @param query A {@link Selector} query
	 * @return the filtered list of elements, or an empty list if none match.
	 */
	Elements select(String query);

	/**
	 * Remove elements from this list that do not match the {@link Selector} query.
	 * <p>
	 * E.g. HTML: {@code <div class=logo>One</div> <div>Two</div>}<br>
	 * <code>Elements divs = doc.select("div").not("#logo");</code><br>
	 * Result: {@code divs: [<div>Two</div>]}
	 * <p>
	 * @param query the selector query whose results should be removed from these elements
	 * @return a new elements list that contains only the filtered results
	 */
	Elements not(String query);

	/**
	 * Get the <i>nth</i> matched element as an Elements object.
	 * <p>
	 * See also {@link #get(int)} to retrieve an Element.
	 * @param index the (zero-based) index of the element in the list to retain
	 * @return Elements containing only the specified element, or, if that element did not exist, an empty list.
	 */
	Elements eq(int index);

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
	Elements parents();

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