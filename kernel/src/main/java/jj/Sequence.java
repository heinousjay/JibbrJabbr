package jj;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Encapsulation of sequence generation
 * @author jason
 *
 */
public class Sequence {
	
	private AtomicLong next = new AtomicLong(0);
	
	/**
	 * Retrieve the next value in the sequence.
	 * @return
	 */
	public String next() {
		return Long.toHexString(next.getAndIncrement()).toUpperCase();
	}
}
