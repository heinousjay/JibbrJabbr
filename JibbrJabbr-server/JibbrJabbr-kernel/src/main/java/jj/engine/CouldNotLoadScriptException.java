package jj.engine;

import java.nio.file.Path;

/**
 * Indicates something went wrong loading a script. The cause should be illuminating
 * @author jason
 *
 */
public class CouldNotLoadScriptException extends RuntimeException {

	private static final long serialVersionUID = 160989540112047885L;

	public CouldNotLoadScriptException(String reason, Exception cause) {
		super(reason, cause);
	}
	
	public CouldNotLoadScriptException(Path path, Exception cause) {
		super(path.toString(), cause);
	}
}
