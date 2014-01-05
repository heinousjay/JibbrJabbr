package jj.engine;

public class SelectorFormatException extends RuntimeException {

	private static final long serialVersionUID = -6319415857235818615L;

	public SelectorFormatException(final String selector) {
		super(String.format("[%s] is not a supported selector format.", selector));
	}
}
