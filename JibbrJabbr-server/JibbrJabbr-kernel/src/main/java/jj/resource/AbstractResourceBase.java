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
	public void addDependent(Resource dependent) {
		assert alive : "cannot accept dependents, i am dead " + toString();
		assert dependent != null : "can not depend on null";
		assert dependent != this : "can not depend on myself";
		dependents.add((AbstractResource)dependent);
	}
	
	@Override
	protected Object[] creationArgs() {
		return EMPTY_ARGS;
	}
}
