package jj.resource;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
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
public class HtmlResource extends AbstractFileResource {

	private final String uri;
	private final Document document;
	
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
		String html = UTF_8.decode(ByteBuffer.wrap(bytes)).toString();
		this.document = Parser.htmlParser().parseInput(html, baseName);
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
