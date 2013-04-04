package jj.jqmessage;

import static jj.StringUtils.*;
// lololololol java
import static jj.jqmessage.JQueryMessage.Type.*;

import java.util.Map;

import jj.Sequence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>
 * Encapsulates a communication between client and server
 * </p>
 * 
 * TODO replace the serialized forms with field tags, after setting up a way of
 * preprocessing js being served to the client so that socket-connect.js (or
 * wherever everything ends up) can use easy-to-read symbols in the source and tags
 * in the execution phase
 * @author jason
 *
 */
public class JQueryMessage {
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	static {
		mapper.setSerializationInclusion(Include.NON_NULL);
	}
	
	public static enum Type {
		/** server --> client append request */
		Append,
		/** server --> client event binding request */
		Bind,
		/** server --> client general invocation, expecting a result */
		Call,
		/** server --> client element creation request, followed by result containing selector */
		Create,
		/** client --> server event fired */
		Event,
		/** client --> server element result */
		Element,
		/** server --> client getter invocation, followed by result containing value */
		Get,
		/** server --> client general invocation, expecting no result! */
		Invoke,
		/** client --> server general string result */
		Result,
		/** server --> client request information in client storage */
		Retrieve,
		/** server --> client setter invocation */
		Set,
		/** server --> client request to store information on the client */
		Store;
	}
	
	private static Sequence ids = new Sequence();
	
	private static String makeId() {
		return String.format("jqm-%s", ids.next());
	}
	
	public static JQueryMessage makeGet(String selector, String type) {
		return makeGet(selector, type, null);
	}
	
	public static JQueryMessage makeGet(String selector, String type, String name) {
		JQueryMessage result = new JQueryMessage(Get);
		result.get().id = makeId();
		result.get().selector = selector;
		result.get().type = type;
		result.get().name = name;
		return result;
	}
	
	public static JQueryMessage makeSet(String selector, String type, String value) {
		return makeSet(selector, type, null, value);
	}
	
	public static JQueryMessage makeSet(String selector, String type, String name, String value) {
		JQueryMessage result = new JQueryMessage(Set);
		result.set().selector = selector;
		result.set().type = type;
		result.set().name = name;
		result.set().value = value;
		return result;
	}
	
	public static JQueryMessage makeInlineCreate(String html, Map<?,?> args) {
		JQueryMessage result = new JQueryMessage(Create);
		result.create().html = html;
		result.create().args = args;
		return result;
	}

	public static JQueryMessage makeCreate(String html, Map<?,?> args) {
		JQueryMessage result = new JQueryMessage(Create);
		result.create().id = makeId();
		result.create().html = html;
		result.create().args = args;
		return result;
	}
	
	public static JQueryMessage makeAppend(String parent, String child) {
		assert !isEmpty(parent) : "append message requires parent";
		assert !isEmpty(child) : "append message requires child";
		JQueryMessage result = new JQueryMessage(Append);
		result.append().parent = parent;
		result.append().child = child;
		return result;
	}
	
	public static JQueryMessage makeBind(String context, String selector, String type) {
		assert !isEmpty(type) : "bind message requires type";
		JQueryMessage result = new JQueryMessage(Bind);
		result.bind().context = context;
		result.bind().selector = selector;
		result.bind().type = type;
		return result;
	}
	
	/**
	 * The Invoke messages invokes a function on the client, expecting a result.
	 * 
	 * @param name the remote function to invoke
	 * @param args the JSON reprepesentation of the arguments array
	 * @return
	 */
	public static JQueryMessage makeInvoke(String name, String args) {
		JQueryMessage result = new JQueryMessage(Invoke);
		result.invoke().id = makeId();
		result.invoke().name = name;
		result.invoke().args = args;
		return result;
	}
	
	/**
	 * The Call message calls a function on the client, expecting no result.
	 * 
	 * @param name the remote function to call
	 * @param args the JSON reprepesentation of the arguments array
	 * @return
	 */
	public static JQueryMessage makeCall(String name, String args) {
		JQueryMessage result = new JQueryMessage(Call);
		result.call().name = name;
		result.call().args = args;
		return result;
	}
	
	public static JQueryMessage makeStore(String key, String value) {
		JQueryMessage result = new JQueryMessage(Store);
		result.store().key = key;
		result.store().value = value;
		return result;
	}
	
	public static JQueryMessage makeRetrieve(String key) {
		JQueryMessage result = new JQueryMessage(Retrieve);
		result.retrieve().id = makeId();
		result.retrieve().key = key;
		return result;
	}
	
	JQueryMessage() {}
	
	JQueryMessage(final Type type) {
		switch(this.type = type) {
		case Append:
			message = new Append();
			break;
		case Bind:
			message = new Bind();
			break;
		case Call:
			message = new Invoke();
			break;
		case Create:
			message = new Create();
			break;
		case Get:
			message = new Get();
			break;
		case Invoke:
			message = new Invoke();
			break;
		case Retrieve:
			message = new Retrieve();
			break;
		case Set:
			message = new Set();
			break;
		case Store:
			message = new Store();
			break;
		default:
			throw new AssertionError("can't create a JQueryMessage of type " + type);
		}
	}
	
	// -- type flag.  used this way to keep memory use efficient 
	// but still with a convenient API for reading/writing
	@JsonIgnore
	private Type type;
	
	@JsonIgnore
	public Type type() {
		return type;
	}
	
	@JsonIgnore
	private Object message;
	
	// --- script messages
	
	@JsonProperty
	public Bind bind() {
		return (Bind)(type == Bind ? message : null);
	}
	
	@JsonProperty
	public Event event() {
		return (Event)(type == Event ? message : null);
	}
	
	@JsonProperty // setters only needed for client -- > server
	void event(Event event) {
		type = Event;
		message = event;
	}
	
	@JsonProperty
	public Element element() {
		return (Element)(type == Element ? message : null);
	}
	
	@JsonProperty
	void element(Element element) {
		type = Element;
		message = element;
	}
	
	@JsonProperty
	public Get get() {
		return (Get)(type == Get ? message : null);
	}
	
	@JsonProperty
	public Set set() {
		return (Set)(type == Set ? message : null);
	}
	
	@JsonProperty
	public Store store() {
		return (Store)(type == Store ? message : null);
	}

	@JsonProperty
	public Retrieve retrieve() {
		return (Retrieve)(type == Retrieve ? message : null);
	}
	
	@JsonProperty
	public Result result() {
		return (Result)(type == Result ? message : null);
	}
	
	@JsonProperty // setters only needed for client -- > server
	void result(Result result) {
		type = Result;
		message = result;
	}

	@JsonProperty
	public Create create() {
		return (Create)(type == Create ? message : null);
	}
	
	@JsonProperty
	public Append append() {
		return (Append)(type == Append ? message : null);
	}
	
	@JsonProperty
	public Invoke invoke() {
		return (Invoke)(type == Invoke ? message : null);
	}
	
	@JsonProperty
	public Invoke call() {
		return (Invoke)(type == Call ? message : null);
	}
	
	public String toString() {
		try {
			return mapper.writeValueAsString(this);
		} catch (Exception e) {
			throw new JQueryMessageException(e);
		}
	}
	
	public static JQueryMessage fromString(String input) {
		try {
			return mapper.readValue(input, JQueryMessage.class);
		} catch (Exception e) {
			throw new JQueryMessageException(e);
		}
	}

	/** indicates if this message expects a result */
	@JsonIgnore
	public boolean expectsResult() {
		return (message instanceof ExpectsResult) && ((ExpectsResult)message).id != null;
	}
	
	@JsonIgnore
	public String resultId() {
		return expectsResult() ? ((ExpectsResult)message).id : null;
	}
	
	@JsonIgnore
	public String id() {
		return (message instanceof ExpectsResult) ? ((ExpectsResult)message).id : null;
	}
	
}
