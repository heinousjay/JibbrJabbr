package jj.api.html;

import java.util.List;

public interface Node extends Cloneable {

	/**
	 Get the node name of this node. Use for debugging purposes and not logic switching (for that, use instanceof).
	 @return node name
	 */
	String nodeName();

	/**
	 Get the base URI of this node.
	 @return base URI
	 */
	String baseUri();

	/**
	 Update the base URI of this node and all of its descendants.
	 @param baseUri base URI to set
	 */
	void setBaseUri(final String baseUri);

	/**
	 * Get an absolute URL from a URL attribute that may be relative (i.e. an <code>&lt;a href></code> or
	 * <code>&lt;img src></code>).
	 * <p/>
	 * E.g.: <code>String absUrl = linkEl.absUrl("href");</code>
	 * <p/>
	 * If the attribute value is already absolute (i.e. it starts with a protocol, like
	 * <code>http://</code> or <code>https://</code> etc), and it successfully parses as a URL, the attribute is
	 * returned directly. Otherwise, it is treated as a URL relative to the element's {@link #baseUri}, and made
	 * absolute using that.
	 * <p/>
	 * As an alternate, you can use the {@link #attr} method with the <code>abs:</code> prefix, e.g.:
	 * <code>String absUrl = linkEl.attr("abs:href");</code>
	 *
	 * @param attributeKey The attribute key
	 * @return An absolute URL if one could be made, or an empty string (not null) if the attribute was missing or
	 * could not be made successfully into a URL.
	 * @see #attr
	 * @see java.net.URL#URL(java.net.URL, String)
	 */
	String absUrl(String attributeKey);

	/**
	 Get a child node by index
	 @param index index of child node
	 @return the child node at this index.
	 */
	Node childNode(int index);

	/**
	 Get this node's children. Presented as an unmodifiable list: new children can not be added, but the child nodes
	 themselves can be manipulated.
	 @return list of children. If no children, returns an empty list.
	 */
	List<Node> childNodes();

	/**
	 Gets this node's parent node.
	 @return parent node; or null if no parent.
	 */
	Node parent();

	/**
	 * Gets the Document associated with this Node. 
	 * @return the Document associated with this Node, or null if there is no such Document.
	 */
	Document ownerDocument();

	/**
	 * Remove (delete) this node from the DOM tree. If this node has children, they are also removed.
	 */
	void remove();

	/**
	 * Insert the specified HTML into the DOM before this node (i.e. as a preceeding sibling).
	 * @param html HTML to add before this node
	 * @return this node, for chaining
	 * @see #after(String)
	 */
	Node before(String html);

	/**
	 * Insert the specified node into the DOM before this node (i.e. as a preceeding sibling).
	 * @param node to add before this node
	 * @return this node, for chaining
	 * @see #after(Node)
	 */
	Node before(Node node);

	/**
	 * Insert the specified HTML into the DOM after this node (i.e. as a following sibling).
	 * @param html HTML to add after this node
	 * @return this node, for chaining
	 * @see #before(String)
	 */
	Node after(String html);

	/**
	 * Insert the specified node into the DOM after this node (i.e. as a following sibling).
	 * @param node to add after this node
	 * @return this node, for chaining
	 * @see #before(Node)
	 */
	Node after(Node node);

	/**
	 Wrap the supplied HTML around this node.
	 @param html HTML to wrap around this element, e.g. {@code <div class="head"></div>}. Can be arbitrarily deep.
	 @return this node, for chaining.
	 */
	Node wrap(String html);

	/**
	 * Removes this node from the DOM, and moves its children up into the node's parent. This has the effect of dropping
	 * the node but keeping its children.
	 * <p/>
	 * For example, with the input html:<br/>
	 * {@code <div>One <span>Two <b>Three</b></span></div>}<br/>
	 * Calling {@code element.unwrap()} on the {@code span} element will result in the html:<br/>
	 * {@code <div>One Two <b>Three</b></div>}<br/>
	 * and the {@code "Two "} {@link TextNode} being returned.
	 * @return the first child of this node, after the node has been unwrapped. Null if the node had no children.
	 * @see #remove()
	 * @see #wrap(String)
	 */
	Node unwrap();

	/**
	 * Replace this node in the DOM with the supplied node.
	 * @param in the node that will will replace the existing node.
	 */
	void replaceWith(Node in);

	/**
	 Retrieves this node's sibling nodes. Effectively, {@link #childNodes()  node.parent.childNodes()}.
	 @return node siblings, including this node
	 */
	List<Node> siblingNodes();

	/**
	 Get this node's next sibling.
	 @return next sibling, or null if this is the last sibling
	 */
	Node nextSibling();

	/**
	 Get this node's previous sibling.
	 @return the previous sibling, or null if this is the first sibling
	 */
	Node previousSibling();

	/**
	 * Get the list index of this node in its node sibling list. I.e. if this is the first node
	 * sibling, returns 0.
	 * @return position in node sibling list
	 * @see jj.api.html.jsoup.nodes.Element#elementSiblingIndex()
	 */
	int siblingIndex();

	/**
	 Get the outer HTML of this node.
	 @return HTML
	 */
	String outerHtml();

	String toString();

	boolean equals(Object o);

	int hashCode();

	/**
	 * Create a stand-alone, deep copy of this node, and all of its children. The cloned node will have no siblings or
	 * parent node. As a stand-alone object, any changes made to the clone or any of its children will not impact the
	 * original node.
	 * <p>
	 * The cloned node may be adopted into another Document or node structure using {@link Element#appendChild(Node)}.
	 * @return stand-alone cloned node
	 */
	Node clone();

}