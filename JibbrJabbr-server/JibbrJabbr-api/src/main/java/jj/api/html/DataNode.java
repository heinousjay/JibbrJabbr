package jj.api.html;

public interface DataNode extends Node {

	/**
	 Get the data contents of this node. Will be unescaped and with original new lines, space etc.
	 @return data
	 */
	String getWholeData();

	/**
	 * Set the data contents of this node.
	 * @param data unencoded data
	 * @return this node, for chaining
	 */
	DataNode setWholeData(String data);

}