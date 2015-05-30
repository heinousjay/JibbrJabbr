package jj.jjmessage;

import javax.inject.Singleton;

import jj.script.ContinuationProcessor;
import jj.script.ContinuationState;

/**
 * going away,this is the wrong level of management, connections themselves are the continuations!
 * @author jason
 *
 */
@Singleton
class JJMessageContinuationProcessor implements ContinuationProcessor {
	@Override
	public void process(ContinuationState continuationState) {}
}
