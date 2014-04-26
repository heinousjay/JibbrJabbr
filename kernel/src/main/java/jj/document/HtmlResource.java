package jj.document;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.document.servable.DocumentConfiguration;
import jj.logging.EmergencyLog;
import jj.resource.AbstractFileResource;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.parser.ParseError;
import org.jsoup.parser.Parser;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

/**
 * An immutable collection of information about an
 * HTML resource.
 * @author jason
 *
 */
@Singleton
public class HtmlResource extends AbstractFileResource {

	private final String uri;
	private final Document document;
	
	private static final class CommentKiller implements NodeVisitor {
		
		List<Node> comments = new ArrayList<>();

		@Override
		public void head(Node node, int depth) {
		}

		@Override
		public void tail(Node node, int depth) {
			if (node instanceof Comment) {
				comments.add(node);
			}
		}
		
		void kill() {
			for (Node node: comments) {
				node.remove();
			}
		}
		
	}
	
	/**
	 * @param absoluteUri The server absolute URI to this resource
	 * @param path The filesystem Path to this resource
	 * @throws IOException
	 */
	@Inject
	HtmlResource(
		final Configuration configuration,
		final EmergencyLog logger,
		final Dependencies dependencies,
		final String name,
		final Path path
	) throws IOException {
		super(dependencies, name, path);
		
		DocumentConfiguration config = configuration.get(DocumentConfiguration.class);
		
		this.uri = name;
		String html = byteBuffer.toString(UTF_8);
		
		Parser parser = Parser.htmlParser().setTrackErrors(config.showParsingErrors() ? Integer.MAX_VALUE : 0);
		
		this.document = parser.parseInput(html, name);
		
		List<ParseError> errors = parser.getErrors();
		if (!errors.isEmpty()) {
			logger.warn("errors while parsing {}, your document may not behave as expected", path);
			
			for (ParseError pe : errors) {
				logger.warn("{}", pe);
			}
			errors.clear();
		}
		
		if (config.removeComments()) {
			CommentKiller commentKiller = new CommentKiller();
			new NodeTraversor(commentKiller).traverse(document);
			commentKiller.kill();
		}
	}
	
	@Override
	public String uri() {
		return uri;
	}
	
	public Document document() {
		return document;
	}
	
	@Override
	public String mime() {
		return "text/html; charset=UTF-8";
	}
}
