package jj.script;

import java.io.Serializable;


public class ContinuationState implements Serializable {
	
	private final Continuation continuation;
	
	private final Class<? extends Continuation> type;
	
	ContinuationState(final Continuation continuation) {
		this.type = continuation.getClass();
		this.continuation = continuation;
	}

	private static final long serialVersionUID = 1L;
	
	public Class<? extends Continuation> type() {
		return type;
	}
	
	public <T extends Continuation> T continuationAs(Class<T> type) {
		assert type.isAssignableFrom(this.type);
		return type.cast(continuation);
	}
	
	public PendingKey pendingKey() {
		return continuation.pendingKey();
	}
	
	public String toString() {
		return new StringBuilder("type: ")
			.append(type)
			.append(", message: ")
			.append(continuation)
			.toString();
	}
}
