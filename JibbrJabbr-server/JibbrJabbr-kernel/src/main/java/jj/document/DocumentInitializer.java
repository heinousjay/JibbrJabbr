package jj.document;

import org.picocontainer.MutablePicoContainer;

public class DocumentInitializer {

	public static MutablePicoContainer initialize(MutablePicoContainer container) {
		
		return container
			.addComponent(ScriptHelperDocumentFilter.class)
			.addComponent(InlineMessagesDocumentFilter.class);
	}
}
