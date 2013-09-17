package jj.resource;

/**
 * This class basically exists to have a shared implementation of
 * dependsOn that can be mocked
 * @author jason
 *
 */
public abstract class AbstractResourceBase extends AbstractResource {

	protected static final Object[] EMPTY_ARGS = {};

	protected AbstractResourceBase(ResourceCacheKey cacheKey) {
		super(cacheKey);
	}
	
	
	@Override
	public void dependsOn(Resource dependency) {
		assert alive : "cannot depend, i am dead " + toString();
		assert dependency != null : "can not depend on null";
		assert dependency != this : "can not depend on myself";
		((AbstractResource)dependency).dependents.add(this);
	}
	
	@Override
	protected Object[] creationArgs() {
		return EMPTY_ARGS;
	}
}
