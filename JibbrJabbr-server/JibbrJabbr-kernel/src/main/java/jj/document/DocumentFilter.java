package jj.document;

/**
 * Internal post-processing for documents
 * 
 * initial intent is to add scripts, deal
 * with message substitutions, and list out
 * events bound during the rendering phase
 * 
 * @author jason
 *
 */
public interface DocumentFilter {
	
	/**
	 * flag indicating this needs to do IO
	 * @return
	 */
	public boolean needsIO(DocumentRequestProcessor documentRequestProcessor);

	public void filter(DocumentRequestProcessor documentRequestProcessor);
}
