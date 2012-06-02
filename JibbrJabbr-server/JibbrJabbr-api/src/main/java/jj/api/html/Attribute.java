package jj.api.html;

public interface Attribute extends Cloneable {

	/**
	 Get the attribute key.
	 @return the attribute key
	 */
	String getKey();

	/**
	 Set the attribute key. Gets normalised as per the constructor method.
	 @param key the new key; must not be null
	 */
	void setKey(String key);

	/**
	 Get the attribute value.
	 @return the attribute value
	 */
	String getValue();

	/**
	 Set the attribute value.
	 @param value the new attribute value; must not be null
	 */
	String setValue(String value);

	/**
	 Get the HTML representation of this attribute; e.g. {@code href="index.html"}.
	 @return HTML
	 */
	String html();

	Attribute clone();

}