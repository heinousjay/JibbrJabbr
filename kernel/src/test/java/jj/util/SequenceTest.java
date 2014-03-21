package jj.util;

import static org.junit.Assert.*;
import jj.util.Sequence;

import org.junit.Test;

public class SequenceTest {

	@Test
	public void test() {
		Sequence sequence = new Sequence();
		assertEquals("0", sequence.next());
		assertEquals("1", sequence.next());
		assertEquals("2", sequence.next());
		assertEquals("3", sequence.next());
		assertEquals("4", sequence.next());
		assertEquals("5", sequence.next());
		assertEquals("6", sequence.next());
		assertEquals("7", sequence.next());
		assertEquals("8", sequence.next());
		assertEquals("9", sequence.next());
		assertEquals("A", sequence.next());
		assertEquals("B", sequence.next());
		assertEquals("C", sequence.next());
		assertEquals("D", sequence.next());
		assertEquals("E", sequence.next());
		assertEquals("F", sequence.next());
		assertEquals("10", sequence.next());

		sequence = new Sequence();
		assertEquals("0", sequence.next());
		assertEquals("1", sequence.next());
		assertEquals("2", sequence.next());
		assertEquals("3", sequence.next());
		assertEquals("4", sequence.next());
		assertEquals("5", sequence.next());
		assertEquals("6", sequence.next());
		assertEquals("7", sequence.next());
		assertEquals("8", sequence.next());
		assertEquals("9", sequence.next());
		assertEquals("A", sequence.next());
		assertEquals("B", sequence.next());
		assertEquals("C", sequence.next());
		assertEquals("D", sequence.next());
		assertEquals("E", sequence.next());
		assertEquals("F", sequence.next());
		assertEquals("10", sequence.next());
	}

}
