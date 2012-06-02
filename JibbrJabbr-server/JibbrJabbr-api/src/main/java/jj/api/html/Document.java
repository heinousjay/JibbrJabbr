package jj.api.html;

public interface Document extends Element {

	String title();
	
	Document title(String title);
	
	Element head();
	
	Element body();
	
	Document clone();
}
