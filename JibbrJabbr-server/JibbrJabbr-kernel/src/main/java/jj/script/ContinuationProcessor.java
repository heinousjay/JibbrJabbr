package jj.script;

interface ContinuationProcessor {

	/** the type of continuation we can restart */
	ContinuationType type();
	
	void process(ContinuationState continuationState);
}
