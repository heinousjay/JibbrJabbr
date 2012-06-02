package fun;

import static java.text.DateFormat.SHORT;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import jj.api.html.Element;
import jj.api.html.Elements;
import jj.api.html.Select;

/**
 * Just sketching out the VERY VERY basic API for manipulating HTML.
 * 
 * Key facts
 *  - new instance of this class is created every time it is needed
 *    - this implies that they should remain lightweight, for instance
 *      encouraging or somehow requiring mostly state
 *  - after making the manipulations, the class is discarded
 *  - the current user's Locale can be injected - this will be determined
 *    by the accept-language header, or some other mechanism
 *  - class is identified by living in the same direct as and
 *    having the same name as the associated .html file
 *  - the @Select annotation performs a selection against that HTML 
 *    file and can return an Elements collection, or the Element that
 *    results from calling {@link Elements#first()}, depending on the
 *    type you ask for.
 *  - the @Select annotation can also decorate the method when there
 *    is only one parameter that can result from the select. Other rules
 *    are still being worked out here.
 *  - if there is no result from the @Select, the method will never be
 *    called.  Should get logged that it wasn't called, though.
 *    
 * note that i spent longer detailing what I want from the code than the code
 * is... also note I may well go with forced-accessible field injections,
 * although the tradeoff is that requires annotations or convention or some
 * sort of determining mechanism.  i do like constructor injection, i do...
 */
public class Example {
	
	private final Locale locale;
	
	public Example(Locale locale) {
		this.locale = locale;
	}
	
	// defining this field will remove all comments from the output
	// although i don't know yet how to do that
	// this should also work per app, per directory, per template
	final boolean removeComments = true;
	
	// figure out a simple looping mechanism

	// accessibility doesn't really matter, private is ignored,
	// but public/protected/package are fine
	// return values are ignored
	// name is more or less documentation (they will be logged)
	// parameter is automatically populated by the container
	// - implies that if no element is found, this method is never called
	// entire object is instantiated to go with the rendering of the page/fragment
	void displayTime(@Select("#currentDate") Element dateSpan) {
		dateSpan.text(DateFormat.getDateInstance(SHORT, locale).format(new Date()));
	}
	
	// removing stuff from the output... pretty simple.
	void cleanParagraphs(@Select("p") Elements paragraphs) {
		paragraphs.remove();
	}
}
