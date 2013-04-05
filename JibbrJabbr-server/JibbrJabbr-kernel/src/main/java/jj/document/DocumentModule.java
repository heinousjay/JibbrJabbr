package jj.document;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;



public class DocumentModule extends AbstractModule {

	@Override
	protected void configure() {
		Multibinder<DocumentFilter> filters = Multibinder.newSetBinder(binder(), DocumentFilter.class);
		
		filters.addBinding().to(ScriptHelperDocumentFilter.class);
		filters.addBinding().to(InlineMessagesDocumentFilter.class);
	}
}
