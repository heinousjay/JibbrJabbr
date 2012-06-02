package jj.api.html;

import java.util.List;
import java.util.Map;

public interface Attributes extends Iterable<Attribute>, Cloneable {

	String get(String key);
	
	void put(String key, String value);
	
	void put(Attribute attribute);
	
	void remove(String key);
	
	boolean hasKey(String key);
	
	int size();
	
	void addAll(Attributes incoming);
	
	List<Attribute> asList();
	
	Map<String, String> dataset();
	
	Attributes clone();
}
