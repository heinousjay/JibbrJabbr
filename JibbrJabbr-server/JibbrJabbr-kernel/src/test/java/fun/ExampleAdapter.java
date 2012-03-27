package fun;

import org.jsoup.nodes.Element;
import org.slf4j.Logger;

// this should probably descend from a base class with
// utilities (like a comment cleaner)
// note that it will always get generated into the package
// with the class it's adapting (saves us the worry of
// accessibility)
public class ExampleAdapter {
	
	private final Logger logger;
	private final Example adapted;
	private final Element element;
	
	public ExampleAdapter(final Logger logger, final Example example, final Element element) {
		this.logger = logger;
		this.adapted = example;
		this.element = element;
	}
	
	// more or less what needs to get generated to make this work
	// open questions 
	// - is this stateless?
	// - should execute merely receive the "interesting" element and
	//   perform its manipulations?
	// - should the adapter be constructed with the Example and HTMLFragment
	//   instances and execute returns the Element... probably.  The main
	//   thinking is that Example might have per-request dependencies and so
	//   clearly cannot be constructed in this method by static code.
	public Element execute() {
		
		// each "selection" method from the adapted class gets called
		// in source order? sure, if ASM supports that.  otherwise note
		// that it's arbitrary, so keep interdependencies down.
		// (which should be fine, simply declare a method that takes all the
		//  element sets you need and does the manipulations internally if
		//  it comes to that)
		logger.trace("Calling displayTime");
		adapted.displayTime(element.select("#currentDate").first());
		
		logger.trace("Calling cleanParagraphs");
		adapted.cleanParagraphs(element.select("p"));
		
		return element;
	}
}
