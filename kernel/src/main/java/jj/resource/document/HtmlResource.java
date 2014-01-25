package jj.resource.document;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.http.server.servable.document.DocumentConfiguration;
import jj.logging.EmergencyLogger;
import jj.resource.AbstractFileResource;
import jj.resource.ResourceCacheKey;

import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.parser.ParseError;
import org.jsoup.parser.Parser;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.slf4j.Logger;

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
	
	private static final class CommentNodeFinder implements NodeVisitor {

		private final HashSet<Comment> comments = new HashSet<>();
		
		@Override
		public void head(Node node, int depth) {
			if (node instanceof Comment) {
				comments.add((Comment)node);
			}
		}

		@Override
		public void tail(Node node, int depth) {
			// nothing to do
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
		final @EmergencyLogger Logger logger,
		final ResourceCacheKey cacheKey,
		final String baseName,
		final Path path
	) throws IOException {
		super(cacheKey, baseName, path);
		
		DocumentConfiguration config = configuration.get(DocumentConfiguration.class);
		
		this.uri = baseName;
		String html = byteBuffer.toString(UTF_8);
		
		Parser parser = Parser.htmlParser().setTrackErrors(config.showParsingErrors() ? Integer.MAX_VALUE : 0);
		
		this.document = parser.parseInput(html, baseName);
		
		List<ParseError> errors = parser.getErrors();
		if (!errors.isEmpty()) {
			logger.warn("errors while parsing {}, your document may not behave as expected", path);
			
			for (ParseError pe : errors) {
				logger.warn("{}", pe);
			}
			errors.clear();
		}
		
		if (config.removeComments()) {
			CommentNodeFinder commentNodeFinder = new CommentNodeFinder();
			new NodeTraversor(commentNodeFinder).traverse(document);
			for (Comment comment : commentNodeFinder.comments) {
				comment.remove();
			}
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