package jj.script;

import java.io.Serializable;

class ContinuationState implements Serializable {
	
	private final Continuable continuable;
	
	private final Class<? extends Continuable> type;
	
	ContinuationState(final Continuable continuable) {
		this.type = continuable.getClass();
		this.continuable = continuable;
	}

	private static final long serialVersionUID = 1L;
	
	public Class<? extends Continuable> type() {
		return type;
	}
	
	public <T extends Continuable> T continuableAs(Class<T> type) {
		assert type.isAssignableFrom(this.type);
		return type.cast(continuable);
	}
	
	public ContinuationPendingKey pendingKey() {
		return continuable.pendingKey();
	}
	
	public String toString() {
		return new StringBuilder("type: ")
			.append(type)
			.append(", message: ")
			.append(continuable)
			.toString();
	}
}
