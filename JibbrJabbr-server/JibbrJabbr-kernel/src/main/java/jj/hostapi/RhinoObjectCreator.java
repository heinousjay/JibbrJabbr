package jj.hostapi;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

public interface RhinoObjectCreator {

	public static final String PROP_CONVERT_ARGS = "//convertArgs";

	Context context();

	ScriptableObject global();

}