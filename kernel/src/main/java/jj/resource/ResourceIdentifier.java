package jj.resource;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A tuple that identifies a given resource, but is not instance specific
 * @author jason
 */
public class ResourceIdentifier<T extends Resource<A>, A> {


	public final Class<T> resourceClass;
	public final Location base;
	public final String name;
	public final A argument;

	private final String stringRep;

	ResourceIdentifier(Class<T> resourceClass, Location base, String name, A argument) {
		this.resourceClass = resourceClass;
		this.base = base;
		this.name = name;
		this.argument = argument;

		stringRep = stringify(resourceClass, base, name, argument);
	}

	private String stringify(Class<?> resourceClass, Location base, String name, Object argument) {
		return namifyType(resourceClass) +
			"@" +base +"/" + name +
			(argument == null ? "" : "{" + argument + "}");
	}

	private String namifyType(Class<?> resourceClass) {
		String name = resourceClass.getCanonicalName();
		StringBuilder out = new StringBuilder(name.length());
		for (int i = 0; i < name.length();) {
			int next = name.indexOf('.', i);
			if (next == -1) {
				out.append(name.substring(i));
				break;
			}

			out.append(name.charAt(i)).append('.');
			i = next + 1;
		}

		return out.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ResourceIdentifier && stringRep.equals(obj.toString());
	}

	public boolean equals(Class<?> resourceClass, Location base, String name, Object argument) {
		return stringRep.equals(stringify(resourceClass, base, name, argument));
	}

	@Override
	public String toString() {
		return stringRep;
	}

	@Override
	public int hashCode() {
		return stringRep.hashCode();
	}
}
