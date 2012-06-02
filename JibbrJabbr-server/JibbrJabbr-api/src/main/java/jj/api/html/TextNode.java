package jj.api.html;

public interface TextNode extends Node {

	/**
	 * Get the text content of this text node.
	 * @return Unencoded, normalised text.
	 * @see TextNode#getWholeText()
	 */
	String text();

	/**
	 * Set the text content of this text node.
	 * @param text unencoded text
	 * @return this, for chaining
	 */
	TextNode text(String text);

	/**
	 Get the (unencoded) text of this text node, including any newlines and spaces present in the original.
	 @return text
	 */
	String getWholeText();

	/**
	 Test if this text node is blank -- that is, empty or only whitespace (including newlines).
	 @return true if this document is empty or only whitespace, false if it contains any text content.
	 */
	boolean isBlank();

	/**
	 * Split this text node into two nodes at the specified string offset. After splitting, this node will contain the
	 * original text up to the offset, and will have a new text node sibling containing the text after the offset.
	 * @param offset string offset point to split node at.
	 * @return the newly created text node containing the text after the offset.
	 */
	TextNode splitText(int offset);

}