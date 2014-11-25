package jj.testing;

import org.junit.runners.model.Statement;

abstract class JibbrJabbrTestStatement extends Statement {
	
	JibbrJabbrTestStatement() {}
	
	JibbrJabbrTestStatement(JibbrJabbrTestStatement inner) {
		inner(inner);
	}
	
	private JibbrJabbrTestStatement inner;
	
	void evaluateInner() throws Throwable {
		if (inner != null) { inner.evaluate(); }
	}
	
	JibbrJabbrTestStatement inner(JibbrJabbrTestStatement inner) {
		if (this.inner != null) inner.inner(this.inner);
		this.inner = inner;
		return this;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + (inner == null ? "" : " wrapping " + inner);
	}
}
