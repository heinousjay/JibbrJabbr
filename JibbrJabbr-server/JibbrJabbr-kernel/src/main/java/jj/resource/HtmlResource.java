package jj.resource;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;

import jj.IOThread;

import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

/**
 * An immutable collection of information about an
 * HTML resource.
 * @author jason
 *
 */
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
	@IOThread
	HtmlResource(
		final String baseName,
		final Path path
	) throws IOException {
		super(baseName, path);
		this.uri = baseName;
		String html = byteBuffer.toString(UTF_8);
		this.document = Parser.htmlParser().parseInput(html, baseName);
		CommentNodeFinder commentNodeFinder = new CommentNodeFinder();
		new NodeTraversor(commentNodeFinder).traverse(document);
		for (Comment comment : commentNodeFinder.comments) {
			comment.remove();
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
