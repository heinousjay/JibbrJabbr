package jj.api.html;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public interface Element extends Node {

	String nodeName();

	/**
	 * Get the name of the tag for this element. E.g. {@code div}
	 * 
	 * @return the tag name
	 */
	String tagName();

	/**
	 * Change the tag of this element. For example, convert a {@code <span>} to a {@code <div>} with
	 * {@code el.tagName("div");}.
	 *
	 * @param tagName new tag name for this element
	 * @return this element, for chaining
	 */
	Element tagName(String tagName);

	/**
	 * Test if this element is a block-level element. (E.g. {@code <div> == true} or an inline element
	 * {@code <p> == false}).
	 * 
	 * @return true if block, false if not (and thus inline)
	 */
	boolean isBlock();

	/**
	 * Get the {@code id} attribute of this element.
	 * 
	 * @return The id attribute, if present, or an empty string if not.
	 */
	String id();

	/**
	 * Get an attribute's value by its key.
	 * <p/>
	 * To get an absolute URL from an attribute that may be a relative URL, prefix the key with <code><b>abs</b></code>,
	 * which is a shortcut to the {@link #absUrl} method.
	 * E.g.: <blockquote><code>String url = a.attr("abs:href");</code></blockquote>
	 * @param attributeKey The attribute key.
	 * @return The attribute, or empty string if not present (to avoid nulls).
	 * @see #attributes()
	 * @see #hasAttr(String)
	 * @see #absUrl(String)
	 */
	String attr(String attributeKey);

	/**
	 * Get all of the element's attributes.
	 * @return attributes (which implements iterable, in same order as presented in original HTML).
	 */
	Attributes attributes();

	/**
	 * Set an attribute value on this element. If this element already has an attribute with the
	 * key, its value is updated; otherwise, a new attribute is added.
	 * 
	 * @return this element
	 */
	Element attr(String attributeKey, String attributeValue);
	
	/**
	 * Test if this element has an attribute.
	 * @param attributeKey The attribute key to check.
	 * @return true if the attribute exists, false if not.
	 */
	boolean hasAttr(String attributeKey);

	/**
	 * Remove an attribute from this element.
	 * @param attributeKey The attribute to remove.
	 * @return this (for chaining)
	 */
	Node removeAttr(String attributeKey);

	/**
	 * Get this element's HTML5 custom data attributes. Each attribute in the element that has a key
	 * starting with "data-" is included the dataset.
	 * <p>
	 * E.g., the element {@code <div data-package="jsoup" data-language="Java" class="group">...} has the dataset
	 * {@code package=jsoup, language=java}.
	 * <p>
	 * This map is a filtered view of the element's attribute map. Changes to one map (add, remove, update) are reflected
	 * in the other map.
	 * <p>
	 * You can find elements that have data attributes using the {@code [^data-]} attribute key prefix selector.
	 * @return a map of {@code key=value} custom data attributes.
	 */
	Map<String, String> dataset();

	Element parent();

	/**
	 * Get this element's parent and ancestors, up to the document root.
	 * @return this element's stack of parents, closest first.
	 */
	Elements parents();

	/**
	 * Get a child element of this element, by its 0-based index number.
	 * <p/>
	 * Note that an element can have both mixed Nodes and Elements as children. This method inspects
	 * a filtered list of children that are elements, and the index is based on that filtered list.
	 * 
	 * @param index the index number of the element to retrieve
	 * @return the child element, if it exists, or {@code null} if absent.
	 * @see #childNode(int)
	 */
	Element child(int index);

	/**
	 * Get this element's child elements.
	 * <p/>
	 * This is effectively a filter on {@link #childNodes()} to get Element nodes.
	 * @return child elements. If this element has no children, returns an
	 * empty list.
	 * @see #childNodes()
	 */
	Elements children();

	/**
	 * Get this element's child text nodes. The list is unmodifiable but the text nodes may be manipulated.
	 * <p/>
	 * This is effectively a filter on {@link #childNodes()} to get Text nodes.
	 * @return child text nodes. If this element has no text nodes, returns an
	 * empty list.
	 * <p/>
	 * For example, with the input HTML: {@code <p>One <span>Two</span> Three <br> Four</p>} with the {@code p} element selected:
	 * <ul>
	 *     <li>{@code p.text()} = {@code "One Two Three Four"}</li>
	 *     <li>{@code p.ownText()} = {@code "One Three Four"}</li>
	 *     <li>{@code p.children()} = {@code Elements[<span>, <br>]}</li>
	 *     <li>{@code p.childNodes()} = {@code List<Node>["One ", <span>, " Three ", <br>, " Four"]}</li>
	 *     <li>{@code p.textNodes()} = {@code List<TextNode>["One ", " Three ", " Four"]}</li>
	 * </ul>
	 */
	List<TextNode> textNodes();

	/**
	 * Get this element's child data nodes. The list is unmodifiable but the data nodes may be manipulated.
	 * <p/>
	 * This is effectively a filter on {@link #childNodes()} to get Data nodes.
	 * @return child data nodes. If this element has no data nodes, returns an
	 * empty list.
	 * @see #data()
	 */
	List<DataNode> dataNodes();

	/**
	 * Find elements that match the {@link Selector} CSS query, with this element as the starting context. Matched elements
	 * may include this element, or any of its children.
	 * <p/>
	 * This method is generally more powerful to use than the DOM-type {@code getElementBy*} methods, because
	 * multiple filters can be combined, e.g.:
	 * <ul>
	 * <li>{@code el.select("a[href]")} - finds links ({@code a} tags with {@code href} attributes)
	 * <li>{@code el.select("a[href*=example.com]")} - finds links pointing to example.com (loosely)
	 * </ul>
	 * <p/>
	 * See the query syntax documentation in {@link org.jsoup.select.Selector}.
	 *
	 * @param cssQuery a {@link Selector} CSS-like query
	 * @return elements that match the query (empty if none match)
	 * @see org.jsoup.select.Selector
	 */
	Elements select(String cssQuery);

	/**
	 * Add a node child node to this element.
	 * 
	 * @param child node to add. Must not already have a parent.
	 * @return this element, so that you can add more child nodes or elements.
	 */
	Element appendChild(Node child);

	/**
	 * Add a node to the start of this element's children.
	 * 
	 * @param child node to add. Must not already have a parent.
	 * @return this element, so that you can add more child nodes or elements.
	 */
	Element prependChild(Node child);

	/**
	 * Create a new element by tag name, and add it as the last child.
	 * 
	 * @param tagName the name of the tag (e.g. {@code div}).
	 * @return the new element, to allow you to add content to it, e.g.:
	 *  {@code parent.appendElement("h1").attr("id", "header").text("Welcome");}
	 */
	Element appendElement(String tagName);

	/**
	 * Create a new element by tag name, and add it as the first child.
	 * 
	 * @param tagName the name of the tag (e.g. {@code div}).
	 * @return the new element, to allow you to add content to it, e.g.:
	 *  {@code parent.prependElement("h1").attr("id", "header").text("Welcome");}
	 */
	Element prependElement(String tagName);

	/**
	 * Create and append a new TextNode to this element.
	 * 
	 * @param text the unencoded text to add
	 * @return this element
	 */
	Element appendText(String text);

	/**
	 * Create and prepend a new TextNode to this element.
	 * 
	 * @param text the unencoded text to add
	 * @return this element
	 */
	Element prependText(String text);

	/**
	 * Add inner HTML to this element. The supplied HTML will be parsed, and each node appended to the end of the children.
	 * @param html HTML to add inside this element, after the existing HTML
	 * @return this element
	 * @see #html(String)
	 */
	Element append(String html);

	/**
	 * Add inner HTML into this element. The supplied HTML will be parsed, and each node prepended to the start of the element's children.
	 * @param html HTML to add inside this element, before the existing HTML
	 * @return this element
	 * @see #html(String)
	 */
	Element prepend(String html);

	/**
	 * Insert the specified HTML into the DOM before this element (i.e. as a preceeding sibling).
	 *
	 * @param html HTML to add before this element
	 * @return this element, for chaining
	 * @see #after(String)
	 */
	Element before(String html);

	/**
	 * Insert the specified node into the DOM before this node (i.e. as a preceeding sibling).
	 * @param node to add before this element
	 * @return this Element, for chaining
	 * @see #after(Node)
	 */
	Element before(Node node);

	/**
	 * Insert the specified HTML into the DOM after this element (i.e. as a following sibling).
	 *
	 * @param html HTML to add after this element
	 * @return this element, for chaining
	 * @see #before(String)
	 */
	Element after(String html);

	/**
	 * Insert the specified node into the DOM after this node (i.e. as a following sibling).
	 * @param node to add after this element
	 * @return this element, for chaining
	 * @see #before(Node)
	 */
	Element after(Node node);

	/**
	 * Remove all of the element's child nodes. Any attributes are left as-is.
	 * @return this element
	 */
	Element empty();

	/**
	 * Wrap the supplied HTML around this element.
	 *
	 * @param html HTML to wrap around this element, e.g. {@code <div class="head"></div>}. Can be arbitrarily deep.
	 * @return this element, for chaining.
	 */
	Element wrap(String html);

	/**
	 * Get sibling elements.
	 * @return sibling elements
	 */
	Elements siblingElements();

	/**
	 * Gets the next sibling element of this element. E.g., if a {@code div} contains two {@code p}s, 
	 * the {@code nextElementSibling} of the first {@code p} is the second {@code p}.
	 * <p/>
	 * This is similar to {@link #nextSibling()}, but specifically finds only Elements
	 * @return the next element, or null if there is no next element
	 * @see #previousElementSibling()
	 */
	Element nextElementSibling();

	/**
	 * Gets the previous element sibling of this element.
	 * @return the previous element, or null if there is no previous element
	 * @see #nextElementSibling()
	 */
	Element previousElementSibling();

	/**
	 * Gets the first element sibling of this element.
	 * @return the first sibling that is an element (aka the parent's first element child) 
	 */
	Element firstElementSibling();

	/**
	 * Get the list index of this element in its element sibling list. I.e. if this is the first element
	 * sibling, returns 0.
	 * @return position in element sibling list
	 */
	Integer elementSiblingIndex();

	/**
	 * Gets the last element sibling of this element
	 * @return the last sibling that is an element (aka the parent's last element child) 
	 */
	Element lastElementSibling();

	/**
	 * Finds elements, including and recursively under this element, with the specified tag name.
	 * @param tagName The tag name to search for (case insensitively).
	 * @return a matching unmodifiable list of elements. Will be empty if this element and none of its children match.
	 */
	Elements getElementsByTag(String tagName);

	/**
	 * Find an element by ID, including or under this element.
	 * <p>
	 * Note that this finds the first matching ID, starting with this element. If you search down from a different
	 * starting point, it is possible to find a different element by ID. For unique element by ID within a Document,
	 * use {@link Document#getElementById(String)}
	 * @param id The ID to search for.
	 * @return The first matching element by ID, starting with this element, or null if none found.
	 */
	Element getElementById(String id);

	/**
	 * Find elements that have this class, including or under this element. Case insensitive.
	 * <p>
	 * Elements can have multiple classes (e.g. {@code <div class="header round first">}. This method
	 * checks each class, so you can find the above with {@code el.getElementsByClass("header");}.
	 * 
	 * @param className the name of the class to search for.
	 * @return elements with the supplied class name, empty if none
	 * @see #hasClass(String)
	 * @see #classNames()
	 */
	Elements getElementsByClass(String className);

	/**
	 * Find elements that have a named attribute set. Case insensitive.
	 *
	 * @param key name of the attribute, e.g. {@code href}
	 * @return elements that have this attribute, empty if none
	 */
	Elements getElementsByAttribute(String key);

	/**
	 * Find elements that have an attribute name starting with the supplied prefix. Use {@code data-} to find elements
	 * that have HTML5 datasets.
	 * @param keyPrefix name prefix of the attribute e.g. {@code data-}
	 * @return elements that have attribute names that start with with the prefix, empty if none.
	 */
	Elements getElementsByAttributeStarting(String keyPrefix);

	/**
	 * Find elements that have an attribute with the specific value. Case insensitive.
	 * 
	 * @param key name of the attribute
	 * @param value value of the attribute
	 * @return elements that have this attribute with this value, empty if none
	 */
	Elements getElementsByAttributeValue(String key, String value);

	/**
	 * Find elements that either do not have this attribute, or have it with a different value. Case insensitive.
	 * 
	 * @param key name of the attribute
	 * @param value value of the attribute
	 * @return elements that do not have a matching attribute
	 */
	Elements getElementsByAttributeValueNot(String key, String value);

	/**
	 * Find elements that have attributes that start with the value prefix. Case insensitive.
	 * 
	 * @param key name of the attribute
	 * @param valuePrefix start of attribute value
	 * @return elements that have attributes that start with the value prefix
	 */
	Elements getElementsByAttributeValueStarting(String key, String valuePrefix);

	/**
	 * Find elements that have attributes that end with the value suffix. Case insensitive.
	 * 
	 * @param key name of the attribute
	 * @param valueSuffix end of the attribute value
	 * @return elements that have attributes that end with the value suffix
	 */
	Elements getElementsByAttributeValueEnding(String key, String valueSuffix);

	/**
	 * Find elements that have attributes whose value contains the match string. Case insensitive.
	 * 
	 * @param key name of the attribute
	 * @param match substring of value to search for
	 * @return elements that have attributes containing this text
	 */
	Elements getElementsByAttributeValueContaining(String key, String match);

	/**
	 * Find elements that have attributes whose values match the supplied regular expression.
	 * @param key name of the attribute
	 * @param pattern compiled regular expression to match against attribute values
	 * @return elements that have attributes matching this regular expression
	 */
	Elements getElementsByAttributeValueMatching(String key, Pattern pattern);

	/**
	 * Find elements that have attributes whose values match the supplied regular expression.
	 * @param key name of the attribute
	 * @param regex regular expression to match agaisnt attribute values. You can use <a href="http://java.sun.com/docs/books/tutorial/essential/regex/pattern.html#embedded">embedded flags</a> (such as (?i) and (?m) to control regex options.
	 * @return elements that have attributes matching this regular expression
	 */
	Elements getElementsByAttributeValueMatching(String key, String regex);

	/**
	 * Find elements whose sibling index is less than the supplied index.
	 * @param index 0-based index
	 * @return elements less than index
	 */
	Elements getElementsByIndexLessThan(int index);

	/**
	 * Find elements whose sibling index is greater than the supplied index.
	 * @param index 0-based index
	 * @return elements greater than index
	 */
	Elements getElementsByIndexGreaterThan(int index);

	/**
	 * Find elements whose sibling index is equal to the supplied index.
	 * @param index 0-based index
	 * @return elements equal to index
	 */
	Elements getElementsByIndexEquals(int index);

	/**
	 * Find elements that contain the specified string. The search is case insensitive. The text may appear directly
	 * in the element, or in any of its descendants.
	 * @param searchText to look for in the element's text
	 * @return elements that contain the string, case insensitive.
	 * @see Element#text()
	 */
	Elements getElementsContainingText(String searchText);

	/**
	 * Find elements that directly contain the specified string. The search is case insensitive. The text must appear directly
	 * in the element, not in any of its descendants.
	 * @param searchText to look for in the element's own text
	 * @return elements that contain the string, case insensitive.
	 * @see Element#ownText()
	 */
	Elements getElementsContainingOwnText(String searchText);

	/**
	 * Find elements whose text matches the supplied regular expression.
	 * @param pattern regular expression to match text against
	 * @return elements matching the supplied regular expression.
	 * @see Element#text()
	 */
	Elements getElementsMatchingText(Pattern pattern);

	/**
	 * Find elements whose text matches the supplied regular expression.
	 * @param regex regular expression to match text against. You can use <a href="http://java.sun.com/docs/books/tutorial/essential/regex/pattern.html#embedded">embedded flags</a> (such as (?i) and (?m) to control regex options.
	 * @return elements matching the supplied regular expression.
	 * @see Element#text()
	 */
	Elements getElementsMatchingText(String regex);

	/**
	 * Find elements whose own text matches the supplied regular expression.
	 * @param pattern regular expression to match text against
	 * @return elements matching the supplied regular expression.
	 * @see Element#ownText()
	 */
	Elements getElementsMatchingOwnText(Pattern pattern);

	/**
	 * Find elements whose text matches the supplied regular expression.
	 * @param regex regular expression to match text against. You can use <a href="http://java.sun.com/docs/books/tutorial/essential/regex/pattern.html#embedded">embedded flags</a> (such as (?i) and (?m) to control regex options.
	 * @return elements matching the supplied regular expression.
	 * @see Element#ownText()
	 */
	Elements getElementsMatchingOwnText(String regex);

	/**
	 * Find all elements under this element (including self, and children of children).
	 * 
	 * @return all elements
	 */
	Elements getAllElements();

	/**
	 * Gets the combined text of this element and all its children.
	 * <p>
	 * For example, given HTML {@code <p>Hello <b>there</b> now!</p>}, {@code p.text()} returns {@code "Hello there now!"}
	 *
	 * @return unencoded text, or empty string if none.
	 * @see #ownText()
	 * @see #textNodes()
	 */
	String text();

	/**
	 * Gets the text owned by this element only; does not get the combined text of all children.
	 * <p>
	 * For example, given HTML {@code <p>Hello <b>there</b> now!</p>}, {@code p.ownText()} returns {@code "Hello now!"},
	 * whereas {@code p.text()} returns {@code "Hello there now!"}.
	 * Note that the text within the {@code b} element is not returned, as it is not a direct child of the {@code p} element.
	 *
	 * @return unencoded text, or empty string if none.
	 * @see #text()
	 * @see #textNodes()
	 */
	String ownText();

	/**
	 * Set the text of this element. Any existing contents (text or elements) will be cleared
	 * @param text unencoded text
	 * @return this element
	 */
	Element text(String text);

	/**
	 Test if this element has any text content (that is not just whitespace).
	 @return true if element has non-blank text content.
	 */
	boolean hasText();

	/**
	 * Get the combined data of this element. Data is e.g. the inside of a {@code script} tag.
	 * @return the data, or empty string if none
	 *
	 * @see #dataNodes()
	 */
	String data();

	/**
	 * Gets the literal value of this element's "class" attribute, which may include multiple class names, space
	 * separated. (E.g. on <code>&lt;div class="header gray"></code> returns, "<code>header gray</code>")
	 * @return The literal class attribute, or <b>empty string</b> if no class attribute set.
	 */
	String className();

	/**
	 * Get all of the element's class names. E.g. on element {@code <div class="header gray"}>},
	 * returns a set of two elements {@code "header", "gray"}. Note that modifications to this set are not pushed to
	 * the backing {@code class} attribute; use the {@link #classNames(java.util.Set)} method to persist them.
	 * @return set of classnames, empty if no class attribute
	 */
	Set<String> classNames();

	/**
	 Set the element's {@code class} attribute to the supplied class names.
	 @param classNames set of classes
	 @return this element, for chaining
	 */
	Element classNames(Set<String> classNames);

	/**
	 * Tests if this element has a class. Case insensitive.
	 * @param className name of class to check for
	 * @return true if it does, false if not
	 */
	boolean hasClass(String className);

	/**
	 Add a class name to this element's {@code class} attribute.
	 @param className class name to add
	 @return this element
	 */
	Element addClass(String className);

	/**
	 Remove a class name from this element's {@code class} attribute.
	 @param className class name to remove
	 @return this element
	 */
	Element removeClass(String className);

	/**
	 Toggle a class name on this element's {@code class} attribute: if present, remove it; otherwise add it.
	 @param className class name to toggle
	 @return this element
	 */
	Element toggleClass(String className);

	/**
	 * Get the value of a form element (input, textarea, etc).
	 * @return the value of the form element, or empty string if not set.
	 */
	String val();

	/**
	 * Set the value of a form element (input, textarea, etc).
	 * @param value value to set
	 * @return this element (for chaining)
	 */
	Element val(String value);

	/**
	 * Retrieves the element's inner HTML. E.g. on a {@code <div>} with one empty {@code <p>}, would return
	 * {@code <p></p>}. (Whereas {@link #outerHtml()} would return {@code <div><p></p></div>}.)
	 * 
	 * @return String of HTML.
	 * @see #outerHtml()
	 */
	String html();

	/**
	 * Set this element's inner HTML. Clears the existing HTML first.
	 * @param html HTML to parse and set into this element
	 * @return this element
	 * @see #append(String)
	 */
	Element html(String html);

	Element clone();

}