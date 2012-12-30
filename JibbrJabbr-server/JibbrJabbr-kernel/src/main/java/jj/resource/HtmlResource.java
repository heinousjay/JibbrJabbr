package jj.resource;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import jj.IOThread;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

/**
 * An immutable collection of information about an
 * HTML resource.
 * @author jason
 *
 */
public class HtmlResource extends AbstractResource {

	private final String uri;
	private final String absoluteUri;
	private final Document document;
	
	/**
	 * @param absoluteUri The server absolute URI to this resource
	 * @param path The filesystem Path to this resource
	 * @throws IOException
	 */
	@IOThread
	HtmlResource(
		final URI absoluteUri,
		final Path path
	) throws IOException {
		super(extractBaseName(absoluteUri.toString()), path);
		this.uri = absoluteUri.getPath();
		this.absoluteUri = absoluteUri.toString();
		String html = UTF_8.decode(ByteBuffer.wrap(bytes)).toString();
		this.document = Parser.htmlParser().parseInput(html, this.absoluteUri);
	}
	
	private static String extractBaseName(final String absoluteUri) {
		return absoluteUri.substring(absoluteUri.indexOf('/', absoluteUri.indexOf("//") + 2) + 1);
	}
	
	@Override
	public String uri() {
		return uri;
	}
	
	public String absoluteUri() {
		return absoluteUri;
	}
	
	public Document document() {
		return document;
	}
	
	@Override
	public String mime() {
		return "text/html; charset=UTF-8";
	}
}
