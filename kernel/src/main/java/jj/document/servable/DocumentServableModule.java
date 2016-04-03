package jj.document.servable;

import jj.JJModule;

public class DocumentServableModule extends JJModule implements BindsDocumentFilter {

	@Override
	protected void configure() {

		bindDocumentFilter(InlineMessagesDocumentFilter.class);
		bindDocumentFilter(ResourceUrlDocumentFilter.class);
		bindDocumentFilter(ScriptHelperDocumentFilter.class);
	}
}
